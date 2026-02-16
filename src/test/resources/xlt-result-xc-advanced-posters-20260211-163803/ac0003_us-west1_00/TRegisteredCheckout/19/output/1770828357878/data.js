var jsonData = {
  "user": "TRegisteredCheckout",
  "date": 1770828419838,
  "actions": [
    {
      "fileName": "pages/0000-Login_US.html",
      "name": "Login_US",
      "requests": [
        {
          "fileName": "responses/0000-Login_US.1-login",
          "startTime": 1770828384169,
          "loadTime": 19414,
          "mimeType": "",
          "name": "login",
          "requestHeaders": [
            {
              "name_": "Cookie",
              "value_": "NINJA_SESSION=60073cdffd397282d73a0c005c889a0b43de50d4-___TS=1770828367968&cart=26c06f17-d8c5-4698-a76e-3e7b144fc911; NINJA_LANG=en-US"
            },
            {
              "name_": "Origin",
              "value_": "https://posters.xceptance.io:8443"
            },
            {
              "name_": "Accept",
              "value_": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            },
            {
              "name_": "Priority",
              "value_": "u=0, i"
            },
            {
              "name_": "User-Agent",
              "value_": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0; Xceptance LoadTest 9.2.2) Gecko/20100101 Firefox/135.0 eHtT1rDLyVLMfHj"
            },
            {
              "name_": "Connection",
              "value_": "keep-alive"
            },
            {
              "name_": "Referer",
              "value_": "https://posters.xceptance.io:8443/en-US/login"
            },
            {
              "name_": "Sec-Fetch-Dest",
              "value_": "document"
            },
            {
              "name_": "Sec-Fetch-Site",
              "value_": "same-origin"
            },
            {
              "name_": "Host",
              "value_": "posters.xceptance.io:8443"
            },
            {
              "name_": "Accept-Encoding",
              "value_": "gzip, deflate, br"
            },
            {
              "name_": "Sec-Fetch-Mode",
              "value_": "navigate"
            },
            {
              "name_": "Upgrade-Insecure-Requests",
              "value_": "1"
            },
            {
              "name_": "Sec-Fetch-User",
              "value_": "?1"
            },
            {
              "name_": "X-XLT-RequestId",
              "value_": "eHtT1rDLyVLMfHj"
            },
            {
              "name_": "Accept-Language",
              "value_": "en-US,en;q=0.5"
            },
            {
              "name_": "Content-Length",
              "value_": "66"
            },
            {
              "name_": "Content-Type",
              "value_": "application/x-www-form-urlencoded"
            }
          ],
          "requestMethod": "POST",
          "requestParameters": [
            {
              "name_": "email",
              "value_": "xce5dcd812a15247@example.com"
            },
            {
              "name_": "password",
              "value_": "XC42-xc42"
            },
            {
              "name_": "btnSignIn",
              "value_": ""
            }
          ],
          "responseCode": 303,
          "responseHeaders": [
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:25 GMT"
            },
            {
              "name_": "Set-Cookie",
              "value_": "NINJA_FLASH=success=Login+successful.+Have+fun+in+our+shop%21; Path=/"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Set-Cookie",
              "value_": "NINJA_SESSION=ab9f5c6c9ee7e6788deb76bc915438b837b8e19e-___TS=1770828385677&user=77e53805-2ac6-43d4-a2c6-244883882514&cart=15635619-0253-46af-a88d-2c65a0ddaf60; Path=/; Expires=Wed, 11-Feb-2026 17:46:43 GMT; Max-Age=3600; Secure; HttpOnly"
            },
            {
              "name_": "Cache-Control",
              "value_": "no-cache, no-store, max-age=0, must-revalidate"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:43 GMT"
            },
            {
              "name_": "Location",
              "value_": "/en-US/accountOverview"
            },
            {
              "name_": "Content-Length",
              "value_": "0"
            },
            {
              "name_": "Server",
              "value_": "Jetty(9.4.53.v20231009)"
            }
          ],
          "status": "303 - See Other",
          "url": "https://posters.xceptance.io:8443/en-US/login",
          "requestBodyRaw": "",
          "protocol": "HTTP/1.1",
          "formDataEncoding": "application/x-www-form-urlencoded"
        },
        {
          "fileName": "responses/0001-Login_US.2-accountOverview.html",
          "startTime": 1770828403584,
          "loadTime": 3500,
          "mimeType": "text/html",
          "name": "accountOverview",
          "requestHeaders": [
            {
              "name_": "Cookie",
              "value_": "NINJA_LANG=en-US; NINJA_FLASH=success=Login+successful.+Have+fun+in+our+shop%21; NINJA_SESSION=ab9f5c6c9ee7e6788deb76bc915438b837b8e19e-___TS=1770828385677&user=77e53805-2ac6-43d4-a2c6-244883882514&cart=15635619-0253-46af-a88d-2c65a0ddaf60"
            },
            {
              "name_": "Origin",
              "value_": "https://posters.xceptance.io:8443"
            },
            {
              "name_": "Accept",
              "value_": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            },
            {
              "name_": "Priority",
              "value_": "u=0, i"
            },
            {
              "name_": "User-Agent",
              "value_": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0; Xceptance LoadTest 9.2.2) Gecko/20100101 Firefox/135.0 eHtT1rDLyVLMfHj SYOdLYJMhouJyPu"
            },
            {
              "name_": "Connection",
              "value_": "keep-alive"
            },
            {
              "name_": "Referer",
              "value_": "https://posters.xceptance.io:8443/en-US/login"
            },
            {
              "name_": "Sec-Fetch-Dest",
              "value_": "document"
            },
            {
              "name_": "Sec-Fetch-Site",
              "value_": "same-origin"
            },
            {
              "name_": "Host",
              "value_": "posters.xceptance.io:8443"
            },
            {
              "name_": "Accept-Encoding",
              "value_": "gzip, deflate, br"
            },
            {
              "name_": "Sec-Fetch-Mode",
              "value_": "navigate"
            },
            {
              "name_": "Upgrade-Insecure-Requests",
              "value_": "1"
            },
            {
              "name_": "Sec-Fetch-User",
              "value_": "?1"
            },
            {
              "name_": "X-XLT-RequestId",
              "value_": "SYOdLYJMhouJyPu"
            },
            {
              "name_": "Accept-Language",
              "value_": "en-US,en;q=0.5"
            }
          ],
          "requestMethod": "GET",
          "requestParameters": [],
          "responseCode": 200,
          "responseHeaders": [
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:45 GMT"
            },
            {
              "name_": "Set-Cookie",
              "value_": "NINJA_FLASH=; Path=/; Expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Cache-Control",
              "value_": "no-cache, no-store, max-age=0, must-revalidate"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:46 GMT"
            },
            {
              "name_": "Content-Type",
              "value_": "text/html;charset=utf-8"
            },
            {
              "name_": "Vary",
              "value_": "Accept-Encoding, User-Agent"
            },
            {
              "name_": "Content-Encoding",
              "value_": "gzip"
            },
            {
              "name_": "Content-Length",
              "value_": "4022"
            },
            {
              "name_": "Server",
              "value_": "Jetty(9.4.53.v20231009)"
            }
          ],
          "status": "200 - OK",
          "url": "https://posters.xceptance.io:8443/en-US/accountOverview",
          "requestBodyRaw": "",
          "protocol": "HTTP/1.1"
        }
      ]
    },
    {
      "fileName": "pages/0001-ClickACategory_US.html",
      "name": "ClickACategory_US",
      "requests": [
        {
          "fileName": "responses/0002-ClickACategory_US.1-models.DefaultText$254010.html",
          "startTime": 1770828407161,
          "loadTime": 9186,
          "mimeType": "text/html",
          "name": "models.DefaultText%4010?categoryId=12",
          "requestHeaders": [
            {
              "name_": "Cookie",
              "value_": "NINJA_LANG=en-US; NINJA_SESSION=ab9f5c6c9ee7e6788deb76bc915438b837b8e19e-___TS=1770828385677&user=77e53805-2ac6-43d4-a2c6-244883882514&cart=15635619-0253-46af-a88d-2c65a0ddaf60"
            },
            {
              "name_": "Accept",
              "value_": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            },
            {
              "name_": "Priority",
              "value_": "u=0, i"
            },
            {
              "name_": "User-Agent",
              "value_": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0; Xceptance LoadTest 9.2.2) Gecko/20100101 Firefox/135.0 0wAguc8zueFOf0r"
            },
            {
              "name_": "Connection",
              "value_": "keep-alive"
            },
            {
              "name_": "Referer",
              "value_": "https://posters.xceptance.io:8443/en-US/accountOverview"
            },
            {
              "name_": "Sec-Fetch-Dest",
              "value_": "document"
            },
            {
              "name_": "Sec-Fetch-Site",
              "value_": "same-origin"
            },
            {
              "name_": "Host",
              "value_": "posters.xceptance.io:8443"
            },
            {
              "name_": "Accept-Encoding",
              "value_": "gzip, deflate, br"
            },
            {
              "name_": "Sec-Fetch-Mode",
              "value_": "navigate"
            },
            {
              "name_": "Upgrade-Insecure-Requests",
              "value_": "1"
            },
            {
              "name_": "Sec-Fetch-User",
              "value_": "?1"
            },
            {
              "name_": "X-XLT-RequestId",
              "value_": "0wAguc8zueFOf0r"
            },
            {
              "name_": "Accept-Language",
              "value_": "en-US,en;q=0.5"
            }
          ],
          "requestMethod": "GET",
          "requestParameters": [],
          "responseCode": 200,
          "responseHeaders": [
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:49 GMT"
            },
            {
              "name_": "Cache-Control",
              "value_": "no-cache, no-store, max-age=0, must-revalidate"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:55 GMT"
            },
            {
              "name_": "Content-Type",
              "value_": "text/html;charset=utf-8"
            },
            {
              "name_": "Vary",
              "value_": "Accept-Encoding, User-Agent"
            },
            {
              "name_": "Content-Encoding",
              "value_": "gzip"
            },
            {
              "name_": "Content-Length",
              "value_": "5643"
            },
            {
              "name_": "Server",
              "value_": "Jetty(9.4.53.v20231009)"
            }
          ],
          "status": "200 - OK",
          "url": "https://posters.xceptance.io:8443/en-US/category/models.DefaultText%4010?categoryId=12",
          "requestBodyRaw": "",
          "protocol": "HTTP/1.1"
        }
      ]
    },
    {
      "fileName": "pages/0002-ClickProductDetails_US.html",
      "name": "ClickProductDetails_US",
      "requests": [
        {
          "fileName": "responses/0003-ClickProductDetails_US.1-models.DefaultText$2540162.html",
          "startTime": 1770828416441,
          "loadTime": 3381,
          "mimeType": "text/html",
          "name": "models.DefaultText%40162?productId=113",
          "requestHeaders": [
            {
              "name_": "Cookie",
              "value_": "NINJA_LANG=en-US; NINJA_SESSION=ab9f5c6c9ee7e6788deb76bc915438b837b8e19e-___TS=1770828385677&user=77e53805-2ac6-43d4-a2c6-244883882514&cart=15635619-0253-46af-a88d-2c65a0ddaf60"
            },
            {
              "name_": "Accept",
              "value_": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            },
            {
              "name_": "Priority",
              "value_": "u=0, i"
            },
            {
              "name_": "User-Agent",
              "value_": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0; Xceptance LoadTest 9.2.2) Gecko/20100101 Firefox/135.0 FXWANXIYPge1QUc"
            },
            {
              "name_": "Connection",
              "value_": "keep-alive"
            },
            {
              "name_": "Referer",
              "value_": "https://posters.xceptance.io:8443/en-US/category/models.DefaultText%4010?categoryId=12"
            },
            {
              "name_": "Sec-Fetch-Dest",
              "value_": "document"
            },
            {
              "name_": "Sec-Fetch-Site",
              "value_": "same-origin"
            },
            {
              "name_": "Host",
              "value_": "posters.xceptance.io:8443"
            },
            {
              "name_": "Accept-Encoding",
              "value_": "gzip, deflate, br"
            },
            {
              "name_": "Sec-Fetch-Mode",
              "value_": "navigate"
            },
            {
              "name_": "Upgrade-Insecure-Requests",
              "value_": "1"
            },
            {
              "name_": "Sec-Fetch-User",
              "value_": "?1"
            },
            {
              "name_": "X-XLT-RequestId",
              "value_": "FXWANXIYPge1QUc"
            },
            {
              "name_": "Accept-Language",
              "value_": "en-US,en;q=0.5"
            }
          ],
          "requestMethod": "GET",
          "requestParameters": [],
          "responseCode": 500,
          "responseHeaders": [
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:46:56 GMT"
            },
            {
              "name_": "Cache-Control",
              "value_": "no-cache, no-store, max-age=0, must-revalidate"
            },
            {
              "name_": "Expires",
              "value_": "Thu, 01 Jan 1970 00:00:00 GMT"
            },
            {
              "name_": "Date",
              "value_": "Wed, 11 Feb 2026 16:47:00 GMT"
            },
            {
              "name_": "Content-Type",
              "value_": "text/html;charset=utf-8"
            },
            {
              "name_": "Content-Length",
              "value_": "1550"
            },
            {
              "name_": "Server",
              "value_": "Jetty(9.4.53.v20231009)"
            }
          ],
          "status": "500 - Server Error",
          "url": "https://posters.xceptance.io:8443/en-US/productDetail/models.DefaultText%40162?productId=113",
          "requestBodyRaw": "",
          "protocol": "HTTP/1.1"
        }
      ]
    }
  ],
  "valueLog": [
    {
      "name_": "account.US.email",
      "value_": "xce5dcd812a15247@example.com"
    },
    {
      "name_": "account.US.isRegistered",
      "value_": "true"
    },
    {
      "name_": "account.US.password",
      "value_": "XC42-xc42"
    },
    {
      "name_": "account.origin",
      "value_": "RANDOM"
    },
    {
      "name_": "com.xceptance.xlt.random.initValue",
      "value_": "-2624935393287208321"
    }
  ]
}