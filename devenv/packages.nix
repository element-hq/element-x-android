{
  system,
  inputs,
  pkgs,
  ...
}: let
  nvim = inputs.nixvim.lib.neovimWithOverrides system [
    ({config, ...}: {
      langChanger.enable = false;
      plugins.lsp.servers.kotlin_lsp = {
        enable = true;
        package = inputs.tias-nixpkgs.packages.x86_64-linux.kotlin-lsp;
        cmd = ["kotlin-lsp" "--stdio"];
      };
      plugins.treesitter.grammarPackages = with config.plugins.treesitter.package.builtGrammars; [
        kotlin
      ];
    })
  ];
in {
  packages = [nvim pkgs.gradle_9 pkgs.openjdk pkgs.android-studio];
}
