# Twilio Configuration Guide

## Required Twilio Configuration Properties

You need to configure the following properties in `application-local.properties` or `application.properties`:

### 1. Twilio Account SID
```
twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```
- **What it is**: Your Twilio Account SID
- **Where to find it**: Twilio Console Dashboard → Account Info
- **Format**: Starts with "AC" followed by 32 characters

### 2. Twilio Auth Token
```
twilio.auth.token=your_auth_token_here
```
- **What it is**: Your Twilio Authentication Token
- **Where to find it**: Twilio Console Dashboard → Account Info
- **Note**: Keep this secure, never commit to version control

### 3. Twilio Phone Number
```
twilio.phone.number=+1234567890
```
- **What it is**: Your Twilio phone number that will send SMS and make calls
- **Where to find it**: Twilio Console → Phone Numbers → Manage → Active numbers
- **Format**: Must include country code with + prefix (e.g., +1234567890, +442071234567)
- **Note**: This is the "From" number for SMS and calls

### 4. Twilio Voice URL (Optional but Recommended)
```
twilio.voice.url=https://your-server.com/twilio/voice
```
- **What it is**: URL to a TwiML endpoint that defines what the call will say
- **Options**:
  1. **TwiML Bin URL**: Create a TwiML Bin in Twilio Console
  2. **Webhook URL**: Your own server endpoint that returns TwiML
  3. **TwiML String**: Can use Twilio's TwiML API to generate dynamically (requires code changes)

## Setup Steps

### Step 1: Get Twilio Account Credentials
1. Sign up at https://www.twilio.com/ (or log in)
2. Go to Console Dashboard
3. Copy your Account SID and Auth Token

### Step 2: Get a Twilio Phone Number
1. In Twilio Console, go to Phone Numbers → Manage → Buy a number
2. Select a number (or use your trial number)
3. Copy the phone number (with country code)

### Step 3: Set Up Voice URL (TwiML Bin - Simplest Option)

**Option A: Use TwiML Bin (Recommended for testing)**
1. Go to Twilio Console → Runtime → TwiML Bins
2. Create a new TwiML Bin
3. Use this TwiML:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <Response>
       <Say voice="alice">Your bus will arrive in approximately 10 minutes. Please be ready at the pickup point.</Say>
   </Response>
   ```
4. Save and copy the URL (looks like: `https://handler.twilio.com/twiml/EHxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)

**Option B: Use TwiML API (Dynamic messages)**
- Requires creating a REST endpoint in your application that returns TwiML XML
- More complex but allows dynamic messages per passenger

### Step 4: Configure application-local.properties

```properties
# Twilio Configuration
twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
twilio.auth.token=your_auth_token_here
twilio.phone.number=+1234567890
twilio.voice.url=https://handler.twilio.com/twiml/EHxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Example Configuration

```properties
# Twilio Configuration (Example - Replace with your actual values)
twilio.account.sid=ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
twilio.auth.token=your_auth_token_here
twilio.phone.number=+1234567890
twilio.voice.url=https://handler.twilio.com/twiml/EHXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

## Testing

Once configured, the application will:
1. Send SMS messages to passengers when bus is approaching
2. Make voice calls using the TwiML URL
3. Log all activities (check logs for SID confirmation)

## Security Notes

- **Never commit credentials to git**
- Use `application-local.properties` (should be in .gitignore)
- For production, use environment variables or secrets management
- Keep Auth Token secure - it provides full access to your Twilio account

## Trial Account Limitations

If using a Twilio trial account:
- Can only send SMS/calls to verified phone numbers
- Add verified numbers in Twilio Console → Phone Numbers → Verified Caller IDs
- Upgrade account to remove limitations

