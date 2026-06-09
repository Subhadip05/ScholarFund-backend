# Authentication Flow

Below is the sequence diagram detailing the OTP request and JWT verification process:

    [Angular/Flutter App]                  [Spring Boot API]                 [MySQL Database]          [Gmail SMTP]
    |                                       |                                 |                       |
    | 1. POST /register-student             |                                 |                       |
    |    {email, name, phone}               |                                 |                       |
    |-------------------------------------->|                                 |                       |
    |                                       | 2. Save User (isVerified=false) |                       |
    |                                       |-------------------------------->|                       |
    |                                       | 3. Generate 6-digit OTP         |                       |
    |                                       | 4. Save OTP (Expires in 5 min)  |                       |
    |                                       |-------------------------------->|                       |
    |                                       | 5. emailService.sendOtpEmail()  |                       |
    |                                       |-------------------------------------------------------->|
    |                                       |                                 |                       | 6. Email delivered
    |<------------------------------------------------------------------------------------------------|
    | 7. 201 Created (Switch UI)            |                                 |                       |
    |<--------------------------------------|                                 |                       |
    |                                       |                                 |                       |
    =========================================================================================================================
    |                                       |                                 |                       |
    | 8. POST /verify-otp                   |                                 |                       |
    |    {email, otpCode}                   |                                 |                       |
    |-------------------------------------->|                                 |                       |
    |                                       | 9. Check OTP Match & Expiry     |                       |
    |                                       |-------------------------------->|                       |
    |                                       | 10. Update User(isVerified=true)|                       |
    |                                       |-------------------------------->|                       |
    |                                       | 11. Delete used OTP             |                       |
    |                                       |-------------------------------->|                       |
    |                                       | 12. Generate JWT Access Token   |                       |
    |                                       | 13. Generate Refresh Token      |                       |
    |                                       | 14. Save Refresh Token          |                       |
    |                                       |-------------------------------->|                       |
    | 15. 200 OK + { Tokens & Role }        |                                 |                       |
    |<--------------------------------------|                                 |                       |
    |                                       |                                 |                       |