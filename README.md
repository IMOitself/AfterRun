# AfterRun
app that sends command to Termux

| 1 | 2 
|:-------:|:-------:|
| ![screenshot 1](assets/screenshot1.jpg) | ![screenshot 2](assets/screenshot2.jpg)

<br>

## Making it work
- must have [Termux](https://f-droid.org/en/packages/com.termux/) and [Termux:API](https://f-droid.org/en/packages/com.termux.api/ ) installed
- must have run this command on Termux first:
- ```
  pkg install termux-api
  sed -i 's/# allow-external-apps = true/allow-external-apps = true/g' ~/.termux/termux.properties
  termux-setup-storage
  ```

<br><br>

## Troubleshooting
#### app crashes instantly when opened:
- ensure you have [Termux](https://f-droid.org/en/packages/com.termux/) and [Termux:API](https://f-droid.org/en/packages/com.termux.api/ ) installed.
- Why did it happen: ```a termux permission is set on AndroidManifest. The app will crash instantly if termux is not installed```

#### Error: Termux Plugin Execution Command Error
- [SCREENSHOT OF NOTIFICATION]
- you should run this command on Termux first:
- ```
  pkg install termux-api
  sed -i 's/# allow-external-apps = true/allow-external-apps = true/g' ~/.termux/termux.properties
  termux-setup-storage
  ```
- NOTE: this might pop up again even if u already run it. just close the app and retry again
- Why did it happen:```termux:api need some requirements in order to run. like setting allow-external-apps to true in the hidden termux.properties file```

#### Maybe open Termux first?
- [SCREENSHOT OF DIALOG]
- This is perfectly normal!
- just open Termux and go back to the app again.
- Why did it happen:```termux:api needs some requirements to run. like disabling battery optimization and granting draw over apps. Once you satisfy those, you might not get this dialog again```
<br><br><br>

## Edit this AfterRun project
- this is an AIDE project
- which means it can only be build using [AIDE](https://www.android-ide.com/)
- but the code is java so its compatible with Android Studio *if u are satisfied to copy and paste*
- no ```kotlin```, ```java version 8+```, ```appcompat```, ```androidx``` and ```any libraries``` is used. And it must remain not to be
- will have separate md file for this topic someday...
