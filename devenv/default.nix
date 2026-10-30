{
  env = {
    GREET = "devenv";
    GRADLE_USER_HOME = "/tmp/.gradle";
  };

  android = {
    enable = true;
    platforms.version = ["37"];
    buildTools.version = ["36.0.0"];
  };

  enterShell =
    /*
    bash
    */
    ''
      echo "some hello info";
    '';
}
