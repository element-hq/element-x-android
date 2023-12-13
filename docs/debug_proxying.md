# Setup a debug mitm proxy to inspect all the app's network traffic

1) Install mitmproxy: `brew install mitmproxy`.
    1) Launch `mitmweb` from a terminal. It will pop up mitmproxy's web interface in a web browser.
1) Configure Android Emulator.
    1) Launch your android emulator.
    1) Open its settings page and go to Settings -> Proxy (nb this tab isn't visible when running the emu inside the Android Studio window, you need to set it so it runs in its own window).
    1) Disable "Use Android Studio HTTP proxy settings" and pick "Manual proxy configuration".
    1) Set `127.0.0.1` as "Host name" and `8080` as "Port number".
    1) Click "Apply" and verify that "Proxy status" is "Success" and close the settings window.
       <img width="932" alt="Screenshot 2023-10-04 at 14 48 47" src="https://github.com/element-hq/element-x-android/assets/1273124/bf99a053-20b0-42a4-91d3-9602f709f684">
1) Install the mitmproxy CA cert (this is needed to see traffic from java/kotlin code, it's not needed for traffic coming from native code e.g. the matrix-rust-sdk).
    1) Open the emulator Chrome browser app
    1) Go to the url `mitm.it`
    1) Follow the instructions to install the CA cert on Android devices.
       <img width="606" alt="Screenshot 2023-10-04 at 14 51 27" src="https://github.com/element-hq/element-x-android/assets/1273124/5f2b6f27-6958-4ea7-97fe-c7f06d105da5">
1) Slightly modify the Element X app source code.
    1) Go to the `RustMatrixClientFactory.create()` method.
    1) Add `.disableSslVerification()` in the `ClientBuilder` method chain.
1) Build and run the Element X app. 
1) Enjoy, you will see all the traffic in mitmproxy's web interface.
   <img width="1110" alt="Screenshot 2023-10-04 at 14 50 03" src="https://github.com/element-hq/element-x-android/assets/1273124/5d039efd-448d-426c-a384-dbbceb9f33ac">
