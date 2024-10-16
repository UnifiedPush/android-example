# UnifiedPush Example

This application is a generic application to handle notifications using UnifiedPush which can be used to test your setup. It is an example how to use [the UnifiedPush library](https://codeberg.org/UnifiedPush/android-connector).

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="90">](https://f-droid.org/packages/org.unifiedpush.example/)

## Receive notifications from a terminal

You can use this app as a rustic application to receive notifications send from a terminal/a process

#### Via encrypted WebPush requests

Toggle ON "WebPush" before registering on UP-Example, and send WebPush requests.

With a python script for instance:

```py
#!/usr/bin/env python

from pywebpush import webpush
import urllib
import sys

if len(sys.argv) < 2:
  print("Usage: {} message".format(sys.argv[0]))

subinfo = {
  "endpoint": "YOUR_ENDPOINT_HERE",
  "keys": {
    "auth": "AUTH_SECRET_HERE",
    "p256dh": "P256DH_SECRET_HERE"
  }
}

message = "title=UP!&message=" + urllib.parse.quote(' '.join(sys.argv[1::]))

webpush(subinfo, message)
```

To use it: `./notify.py My message here`

Depending on your distributor, you may need to set the VAPID header too: add `headers={"authorization": "vapid t=[...],k=[...]"}` to the webpush call.

#### Via unencrypted requests

Push notifications are intended to be encrypted. But you can send unencrypted requests by sending HTTP POST requests with the header `Content-Encoding: aes128gcm`.
Depending on your distributor, you may need to set the VAPID header too: add `Authorization: vapid t=[...],k=[...]`.

For instance with cURL:

```
curl -X POST $endpoint -H "Content-encoding: aes128gcm" --data "title={Your Title}&message={Your Message}"
```

## Developer mode, to test a distributor

This application can be used to test different features of a distributor. To enable this mode, check `Developer mode` in the upper right menu.

You will be able to:
- Show an error notification if the received message hasn't been correctly decrypted, by checking `Error if decryption fails`.
- Use VAPID
- Send cleartext messages
- Use wrong VAPID keys, you should not receive new messages.
- Use wrong encryption keys, decryption for new messages will fail.
- Start a foreground service when a message is received, by checking `Foreground service on message`. It must work even if the example application has optimized battery, and is in the background.
- Resend registration message, by clicking on `Reregister`.
- Start the link activity using deep link by clicking on `Deep link`.
- Change the distributor, without using the deep link by clicking on `Change distributor`.
- Set urgency for new messages.
- Test TTL. The TTL is correctly implemented if you don't receive the test message. You will have to disconnect the distributor during the process.
- Test topics. The topics are correctly implemented if you don't receive the 1st test message which would have been replaced by the 2nd. You will have to disconnect the distributor during the process.
- Test push while the application is in the background

## Development

#### CI Secrets
* `release_key`: keystore in base64
* `release_store_password`: keystore password
* `release_key_password`: key password, the key alias must be `unifiedpush`
* `codeberg_token`: codeberg token for package, with `write:package` right (https://codeberg.org/user/settings/applications)

# Funding

This project is funded through [NGI Zero Core](https://nlnet.nl/core), a fund established by [NLnet](https://nlnet.nl) with financial support from the European Commission's [Next Generation Internet](https://ngi.eu) program. Learn more at the [NLnet project page](https://nlnet.nl/project/UnifiedPush).

[<img src="https://codeberg.org/UnifiedPush/documentation/raw/branch/main/static/img/nlnet_banner.png" alt="NLnet foundation logo" width="20%" />](https://nlnet.nl)
[<img src="https://codeberg.org/UnifiedPush/documentation/raw/branch/main/static/img/NGI0_tag.svg" alt="NGI Zero Logo" width="20%" />](https://nlnet.nl/core)
