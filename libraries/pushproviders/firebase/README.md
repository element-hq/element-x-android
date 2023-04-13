# Firebase

## Configuration

In order to make this module only know about Firebase, the plugin `com.google.gms.google-services` has been disabled from the `app` module.

To be able to change the values in the file `firebase.xml` from this module, you should enable the plugin `com.google.gms.google-services` again, copy the file `google-services.json` to the folder `/app/src/main`, build the project, and check the generated file `app/build/generated/res/google-services/<buildtype>/values/values.xml` to import the generated values into the `firebase.xml` files.
