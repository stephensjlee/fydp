#include <xc.h>
#include <stdio.h>
#include <stdint.h>
#include <sys/attribs.h>
#include <math.h>

// DEVCFG3
// USERID = No Setting
#pragma config PMDL1WAY = ON            // Peripheral Module Disable Configuration (Allow only one reconfiguration)
#pragma config IOL1WAY = ON             // Peripheral Pin Select Configuration (Allow only one reconfiguration)

// DEVCFG1
#pragma config FNOSC = FRC              // Oscillator Selection Bits (Fast RC Osc) NOTE: code does clock switching
#pragma config FSOSCEN = OFF            // Secondary Oscillator Enable (Disabled)
#pragma config IESO = OFF               // Internal/External Switch Over (Disabled)
#pragma config POSCMOD = OFF            // Primary Oscillator Configuration (Primary osc disabled)
#pragma config OSCIOFNC = ON            // CLKO Output Signal Active on the OSCO Pin (Enabled)
#pragma config FPBDIV = DIV_1           // Peripheral Clock Divisor (Pb_Clk is Sys_Clk/1)
#pragma config FCKSM = CSECMD           // Clock Switching and Monitor Selection (Clock Switch Enable, FSCM Disabled)
#pragma config WDTPS = PS2048           // Watchdog Timer Postscaler (2048 ms)
#pragma config WINDIS = OFF             // Watchdog Timer Window Enable (Watchdog Timer is in Non-Window Mode)
#pragma config FWDTEN = OFF             // Watchdog Timer Enable (WDT Enabled (SWDTEN Bit Controls))
#pragma config FWDTWINSZ = WINSZ_25     // Watchdog Timer Window Size (Window Size is 25%)

// DEVCFG0
#pragma config JTAGEN = OFF             // JTAG Enable (JTAG Disabled)
#pragma config ICESEL = ICS_PGx3        // ICE/ICD Comm Channel Select (Communicate on PGEC3/PGED3)
#pragma config PWP = OFF                // Program Flash Write Protect (Disable)
#pragma config BWP = OFF                // Boot Flash Write Protect bit (Protection Disabled)
#pragma config CP = OFF                 // Code Protect (Protection Disabled)


#define c_assert(e) \
if(!(e)) \
{   \
    printf("%s,%d: assertion '%s' failed\n", __FILE__, __LINE__, #e); \
    device_failure_trap();\
}   \

#define LOOP_FREQUENCY 10

// GPIO defines
#define LED     LATBbits.LATB9
#define ALTI_CS LATBbits.LATB15

// Altimeter commands   
#define ALTI_RST_CMD            (uint8_t)0x1E // Reset altimeter module
#define ALTI_CONV_D1_256        (uint8_t)0x40 // Convert D1, 24 bit result, OSR=256
#define ALTI_CONV_D1_512        (uint8_t)0x42 // Convert D1, 24 bit result, OSR=512
#define ALTI_CONV_D1_1024       (uint8_t)0x44 // Convert D1, 24 bit result, OSR=1024
#define ALTI_CONV_D1_2048       (uint8_t)0x46 // Convert D1, 24 bit result, OSR=2048
#define ALTI_CONV_D1_4096       (uint8_t)0x48 // Convert D1, 24 bit result, OSR=4096
#define ALTI_CONV_D2_256        (uint8_t)0x50 // Convert D2, 24 bit result, OSR=256
#define ALTI_CONV_D2_512        (uint8_t)0x52 // Convert D2, 24 bit result, OSR=512
#define ALTI_CONV_D2_1024       (uint8_t)0x54 // Convert D2, 24 bit result, OSR=1024
#define ALTI_CONV_D2_2048       (uint8_t)0x56 // Convert D2, 24 bit result, OSR=2048
#define ALTI_CONV_D2_4096       (uint8_t)0x58 // Convert D2, 24 bit result, OSR=4096
#define ALTI_READ_ADC           (uint32_t)0x00000000// Wait at least 10ms for 4096 OSR before calling this, 24 bit result

#define ALTI_READ_PROM1         (uint32_t)0xA2000000 // Read C1, 16 bit result
#define ALTI_READ_PROM2         (uint32_t)0xA4000000 // Read C2, 16 bit result
#define ALTI_READ_PROM3         (uint32_t)0xA6000000 // Read C3, 16 bit result
#define ALTI_READ_PROM4         (uint32_t)0xA8000000 // Read C4, 16 bit result
#define ALTI_READ_PROM5         (uint32_t)0xAA000000 // Read C5, 16 bit result
#define ALTI_READ_PROM6         (uint32_t)0xAC000000 // Read C6, 16 bit result
#define ALTI_READ_PROM7         (uint32_t)0xAE000000 // Read C7, 16 bit result

#define SERVO_IN_POS 60
#define SERVO_OUT_POS 43
#define SERVO_POS_MIN 18
#define SERVO_POS_MAX 75

static void setup_mcu(void);
static void setup_alti(void);
static uint8_t send_recv_spi_8b(uint8_t data);
static uint32_t send_recv_spi_32b(uint32_t data);
static void device_failure_trap(void);
static void delay_ms(uint16_t delay);
static int32_t calculate_altitude_ft(uint32_t pressure_mbar_x100);
static int32_t calculate_pressure_mbar_x100(int32_t d1, int32_t d2);

// Persistent altimeter calibration constants and variables
static uint32_t alti_c1 = 0;    // Pressure sensitivity
static uint32_t alti_c2 = 0;    // Pressure offset
static uint32_t alti_c3 = 0;    // Temperature coefficient of pressure sensitivity
static uint32_t alti_c4 = 0;    // Temperature coefficient of pressure offset
static uint32_t alti_c5 = 0;    // Reference temperature
static uint32_t alti_c6 = 0;    // Temperature coefficient of the temperature

static uint8_t servopos = SERVO_IN_POS;
static int32_t alti_gnd = 0;

int main(void)
{
    setup_mcu();
    setup_alti();
    
    while(1)
    {
        ALTI_CS = 0;
        (void)send_recv_spi_8b(ALTI_CONV_D1_4096); // Start D1 conversion
        ALTI_CS = 1;
        delay_ms(10);
        
        ALTI_CS = 0;
        uint32_t d1 = send_recv_spi_32b(ALTI_READ_ADC) & 0x00ffffff; // Read D1 (24 bit value)
        ALTI_CS = 1;
        delay_ms(1);
        
        ALTI_CS = 0;
        (void)send_recv_spi_8b(ALTI_CONV_D2_4096); // Start D2 conversion
        ALTI_CS = 1;
        delay_ms(10);
            
        ALTI_CS = 0;
        uint32_t d2 = send_recv_spi_32b(ALTI_READ_ADC) & 0x00ffffff; // Read D2 (24 bit value)
        ALTI_CS = 1;
        delay_ms(1);
        
        int32_t pressure_mbax_x100 = calculate_pressure_mbar_x100(d1, d2);
        int32_t alti_asl = calculate_altitude_ft(pressure_mbax_x100);

        if(U2STAbits.URXDA)
        {
            char c = U2RXREG;
            _mon_putc(c);
            LED = ~LED;

            if(c == '=')
            {
                servopos++;
            }
            else if(c == '-')
            {
                servopos--;
            }
        }

        if(U2STAbits.OERR)
        {
            U2STAbits.OERR = 0;
        }

        static uint8_t initial = 1;
        if(initial)
        {
            alti_gnd = alti_asl;
            initial = 0;
        }

        alti_gnd = (float)alti_asl*0.01f + (float)alti_gnd*0.99f;
        int32_t alti_agl = alti_asl - alti_gnd;
        
        printf("%lu,%lu,%i,%i,%u,%i,%i\n", d1, d2, pressure_mbax_x100, alti_asl,servopos,alti_gnd, alti_agl);
        
        WDTCONbits.WDTCLR = 1;
        
        OC1RS = servopos;
    }
}

void _mon_putc (char c)
{
    // NOTE: loop timeout is used to be independent from hardware, timeout is ~~20ms
    while (!U2STAbits.TRMT); //Wait 'till previous byte transmission is complete
    
    if(U2STAbits.TRMT) 
    {
        U2TXREG = c;
    }
}

static void setup_mcu(void)
{
    // UART 1 setup
    U1BRG = 0;              // Baud rate = 500k for 8MHz peripheral clock, 250k for 4MHz peripheral clock
    U1MODEbits.ON = 0;      // Turn UART peripheral OFF and reset it;
    asm volatile("nop");    // Can't set UART SFRs in immediate clock cycle after resetting the ON bit
    U1MODEbits.SIDL = 1;    // Stop in IDLE mode
    U1MODEbits.STSEL = 1;   // 2 stop bits
    U1MODEbits.ON = 1;      // Turn UART peripheral OFF and reset it;    
    U1STAbits.UTXEN = 1;    // Transmitter enable, needs to be enabled AFTER U1MODE.ON is set!!
    U1STAbits.URXEN = 1;    // Receiver enable, needs to be enabled AFTER U1MODE.ON is set!!
    
    // UART 2 setup
    U2BRG = 16;             // Baud rate = 117647 for 8MHz peripheral clock and BRGH=1, 250k for 4MHz peripheral clock and BRGH=1
    U2MODEbits.ON = 0;      // Turn UART peripheral OFF and reset it;
    asm volatile("nop");    // Can't set UART SFRs in immediate clock cycle after resetting the ON bit
    U2MODEbits.SIDL = 1;    // Stop in IDLE mode
    U2MODEbits.STSEL = 1;   // 2 stop bits
    U2MODEbits.BRGH = 1;    // High speed mode
    U2MODEbits.ON = 1;      // Turn UART peripheral OFF and reset it;    
    U2STAbits.UTXEN = 1;    // Transmitter enable, needs to be enabled AFTER U1MODE.ON is set!!
    U2STAbits.URXEN = 1;    // Receiver enable, needs to be enabled AFTER U1MODE.ON is set!!
    
    // Timers
    T1CONbits.ON = 0;
    asm volatile("nop");    // Can't set timer SFRs in next clock cycle after clearing ON bit
    T1CONbits.TCKPS = 3;    // We're currently running at 8MHz, so divide clock by 256 to get 31.25KHz
    T1CONbits.ON = 1;
    c_assert(T1CON != 0);
    TMR1 = 0;
    PR1 = 31250/LOOP_FREQUENCY;

    T2CONbits.ON = 0;
    asm volatile("nop");    // Can't set timer SFRs in next clock cycle after clearing ON bit
    T2CONbits.TCKPS = 7;    // Timer used for short delays and timeouts, divide by 256 to get 31.25KHz
    T2CONbits.ON = 1;
    c_assert(T2CON != 0);
    TMR2 = 0;
    
    T3CONbits.ON = 0;
    asm volatile("nop");    // Can't set timer SFRs in next clock cycle after clearing ON bit
    T3CONbits.TCKPS = 7;    // Timer used for short delays and timeouts, divide by 256 to get 31.25KHz
    PR3 = 600;              // Such that the PWM for servo is 52Hz
    T3CONbits.ON = 1;
    c_assert(T3CON != 0);
    TMR3 = 0;
    
    // Output Compare 
    OC1CONbits.OCTSEL = 1; // Timer3 is the clock source for this Output Compare module
    OC1CONbits.OCM = 6;    //  
    OC1RS = SERVO_OUT_POS;
    OC1CONbits.ON = 1;
    
    // Set all ports to digital mode, not analog
    ANSELA = 0;
    ANSELB = 0;
    c_assert(ANSELA == 0);
    c_assert(ANSELB == 0);
    
    // Peripheral pin select mappings
    SYSKEY = 0xAA996655;    // SYSKEY unlock sequence
    SYSKEY = 0x556699AA;
    CFGCONbits.IOLOCK = 0;  // Unlock PPS registers
    RPB13R = 0x3;           // SDO1 => RPB13
    SDI1R = 0x4;            // SDI1 => RPB8
    U1RXR = 0x2;            // U1RX => RPA4
    RPB4R = 0x1;            // U1TX => RPB4
    U2RXR = 0x0;            // U2RX => RPA1
    RPB0R = 0x2;            // U2TX => RPB0
    RPA0R = 0x5;            // OC1 => RPA0
    CFGCONbits.IOLOCK = 1;  // Lock PPS registers. NOTE: from now on, PPS registers cannot be unlocked
    SYSKEY = 0;             // SYSKEY lock
    c_assert(RPB13R == 0x3);
    c_assert(SDI1R == 0x4);
    c_assert(U1RXR == 0x2);
    c_assert(RPB4R == 0x1);
    

    
    // Set output pins high (except LED)
    LATBbits.LATB9 = 1;     // Turn on LED
    LATBbits.LATB15 = 1;    // CS of Altimeter module
    c_assert(LATBbits.LATB9 == 1);
    c_assert(LATBbits.LATB15 == 1);
    
    // Configure output pins as output pins
    TRISBbits.TRISB9 = 0;   // LED
    TRISBbits.TRISB15 = 0;  // CS (Chip Select) of Altimeter module
    TRISAbits.TRISA0 = 0;   // PWM
    c_assert(TRISBbits.TRISB9 == 0);
    c_assert(TRISBbits.TRISB15 == 0);
    
    // SPI setup
    SPI1CONbits.ON = 0;     // Turn SPI peripheral off and reset it
    asm volatile("nop");    // Can't set SPI SFRs in immediate clock cycle after resetting the ON bit
    SPI1CONbits.MCLKSEL = 0;// Use PBCLK for SPI
    SPI1CONbits.SIDL = 1;   // Stop in IDLE mode
    SPI1CONbits.DISSDO = 0; // Enable SDO pin
    SPI1CONbits.MODE16 = 0; // Initially 8 bit mode
    SPI1CONbits.MODE32 = 0;
    SPI1CONbits.SMP = 0;    // Sample in middle of data output time
    SPI1CONbits.CKE = 0;    // SPI Mode 3, both altimeter and LCD work in this mode
    SPI1CONbits.CKP = 1;    // SPI Mode 3
    SPI1CONbits.MSTEN = 1;  // Master mode
    SPI1CONbits.DISSDI = 0; // Enable SDI
    SPI1CONbits.ON = 1;     // Turn SPI peripheral ON
    SPI1BRG = 0;            // Set SPI clock to 4MHz: PBCLK/(2*(1 + 0)))
    c_assert(SPI1CON != 0);
}


static void setup_alti(void)
{
    // Make sure no other SPI devices are selected
    c_assert(ALTI_CS != 0);
    
    // Reset sensor
    ALTI_CS = 0;
    (void)send_recv_spi_8b(ALTI_RST_CMD);
    delay_ms(10u);
    ALTI_CS = 1;
    delay_ms(10u);
    
    // Aquire all calibration constants
    ALTI_CS = 0;
    alti_c1  = (send_recv_spi_32b(ALTI_READ_PROM1) >> 8) & 0xffff;
    printf("alti_c1 = %x\n", alti_c1);
    ALTI_CS = 1;
    delay_ms(1u);

    ALTI_CS = 0;
    alti_c2  = (send_recv_spi_32b(ALTI_READ_PROM2) >> 8) & 0xffff;
    printf("alti_c2 = %x\n", alti_c2);
    ALTI_CS = 1;
    delay_ms(1u);

    ALTI_CS = 0;
    alti_c3  = (send_recv_spi_32b(ALTI_READ_PROM3) >> 8) & 0xffff;
    printf("alti_c3 = %x\n", alti_c3);
    ALTI_CS = 1;
    delay_ms(1u);

    ALTI_CS = 0;
    alti_c4  = (send_recv_spi_32b(ALTI_READ_PROM4) >> 8) & 0xffff;
    printf("alti_c4 = %x\n", alti_c4);
    ALTI_CS = 1;
    delay_ms(1u);

    ALTI_CS = 0;
    alti_c5  = (send_recv_spi_32b(ALTI_READ_PROM5) >> 8) & 0xffff;
    printf("alti_c5 = %x\n", alti_c5);
    ALTI_CS = 1;
    delay_ms(1u);

    ALTI_CS = 0;
    alti_c6  = (send_recv_spi_32b(ALTI_READ_PROM6) >> 8) & 0xffff;
    printf("alti_c6 = %x\n", alti_c6);
    ALTI_CS = 1;
    delay_ms(1u);
}

static uint8_t send_recv_spi_8b(uint8_t data)
{
    volatile uint16_t tm = 0; // Timeout variable
    
    SPI1CONbits.MODE16 = 0; // 8 bit mode
    SPI1CONbits.MODE32 = 0;
    
    // Wait for previous word to be transfered from buffer to shift register
    while(SPI1STATbits.SPITBF && (++tm != 0)); 

    // Clear SPIRBF and set SPIRBE
    (void)SPI1BUF;
       
    if(SPI1STATbits.SPITBE)
    {
        SPI1BUF = data;           
    }
    else
    {
        return 1; // Timeout on SPITBE
    }
    
    // Reset timeout variable
    tm = 0;
    
    // Wait for receive word
    while(SPI1STATbits.SPIRBE && (++tm != 0));
    
    return (uint8_t)SPI1BUF; 
}


static uint32_t send_recv_spi_32b(uint32_t data)
{
    volatile uint16_t tm = 0; // Timeout variable

    SPI1CONbits.MODE16 = 0; // 32 bit mode
    SPI1CONbits.MODE32 = 1;

    // Wait for previous word to be transfered from buffer to shift register
    while(SPI1STATbits.SPITBF && (++tm != 0));

    // Clear SPIRBF and set SPIRBE
    (void)SPI1BUF;

    if(SPI1STATbits.SPITBE)
    {
        SPI1BUF = data;
    }
    else
    {
        return 1; // Timeout on SPITBE
    }

    // Reset timeout variable
    tm = 0;

    // Wait for receive word
    while(SPI1STATbits.SPIRBE && (++tm != 0));

    return (uint32_t)SPI1BUF;
}


// Oh shit function
void device_failure_trap(void)
{
    // Stop 
    asm volatile("di");

    // TODO: handle user input

    // TODO: display debug info

    printf("Device failure, going into infinite loop\n");

    while(1)
    {
        // Watchdog will reset here
    }
}

// This function assumes timer running at 31.25 kHZ
static void delay_ms(uint16_t delay)
{
    TMR2 = 0;
    IFS0bits.T2IF = 0;
    PR2 = 31u*delay;

    // Wait for compare match interrupt flag
    while(!IFS0bits.T2IF);
}

// Calculates pressure from altimeter ADC readings and its' calibration constants
static int32_t calculate_pressure_mbar_x100(int32_t d1, int32_t d2)
{
    // Make sure all calibration constants are valid
    c_assert(alti_c1 != 0);   
    c_assert(alti_c2 != 0);   
    c_assert(alti_c3 != 0);   
    c_assert(alti_c4 != 0);   
    c_assert(alti_c5 != 0);   
    c_assert(alti_c6 != 0);   

    // Range check
    c_assert((d1 >= 0) && (d1 <= 16777216));
    c_assert((d2 >= 0) && (d2 <= 16777216));

    // Difference between actual and reference temperature. Intermediate operations in 32 bit signed
    int64_t dT = d2 - (int64_t)alti_c5*256;

    // Range check
    c_assert((dT >= -16776960) && (dT <= 16777216));

    // Actual temperature (-40...85C with 0.01C resolution). Intermediate operations in 64 bit signed
    int32_t TEMP_x100 = (int32_t)(2000 + (((int64_t)dT*alti_c6)/8388608)); 

    // Range check
    c_assert((TEMP_x100 >= -4000) && (TEMP_x100 <= 8500));

    // Offset at actual temperature. Intermediate operations in 64 bit signed
    int64_t OFF = (int64_t)alti_c2*65536ll + ((int64_t)alti_c4*dT)/128ll;

    // Range check
    c_assert(OFF >= -17179344900 && OFF <= 25769410560);

    // Sensitivity at actual temperature. Intermediate operations in 64 bit signed
    int64_t SENS = (int64_t)alti_c1*32768ll + ((int64_t)alti_c3*(int64_t)dT)/256ll;

    // Range check
    c_assert(SENS >= -8589672450 && SENS <= 12884705280);

    // Temperature compensated pressure (10...1200mbar with 0.01mbar resolution). Intermediate operations in 64 bit signed
    int64_t P_x100 = ((((int64_t)d1*SENS)/2097152ll)-OFF)/32768ll;

    // Range check
    c_assert(P_x100 >= 1000 && P_x100 <= 120000);

    return (int32_t)P_x100; 
}


static int32_t calculate_altitude_ft(uint32_t pressure_mbar_x100)
{
    float Tb = 288.15f;     // Temperature at sea level (K)
    float Lb = -0.0065f;    // Standard temperature lapse rate (K/m)
    float Pb = 1013.25f;    // Pressure at sea level (Pa)
    float R = 8.3143f;      // Universal gas constant ((N*m)/(mol*K))
    float g = 9.8067f;      // gravitational acceleration constant (m/s^2)
    float M = 0.02896f;     // molar mass of Earth's air (kg/mol)

    float tmp = 0.0f;       // Temporary variable

    tmp = (-R*Lb)/(g*M);
    tmp = pow(((float)pressure_mbar_x100/100.0f)/Pb, tmp);
    tmp = tmp - 1.0f;
    tmp = (Tb/Lb)*tmp;      // Result in meters
    tmp = tmp*3.28084f;     // Result converted to feet

    // Check range
    c_assert((tmp < INT32_MAX) && (tmp > INT32_MIN));
    
    return (int32_t)tmp;
}
