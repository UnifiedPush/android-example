# UnifiedPush Example

This application is a generic application to handle notifications using UnifiedPush which can be used to test your setup. It is an example how to use [the UnifiedPush library](https://codeberg.org/UnifiedPush/android-connector).

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="90">](https://f-droid.org/packages/org.unifiedpush.example/)

## Receive notifications from a terminal

You can use this app as a rustic application to receive notifications send from a terminal/a process

#### Via unencrypted requests

Toggle OFF "WebPush" before registering on UP-Example, and send HTTP POST requests.

For instance with cURL:

```
curl -X POST $endpoint --data "title={Your Title}&message={Your Message}"
```

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
