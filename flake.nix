{
  description = "trackgbfs - GBFS v1 archiver and public API";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachSystem [ "x86_64-linux" "aarch64-linux" ] (system:
      let
        pkgs = import nixpkgs { inherit system; };
        graalvm = pkgs.graalvmPackages.graalvm-ce;

        graalvmReachabilityMetadata = pkgs.fetchurl {
          url = "https://github.com/oracle/graalvm-reachability-metadata/releases/download/0.3.34/graalvm-reachability-metadata-0.3.34.zip";
          hash = "sha256-o5maxh3vo16PBVd1AELzM5JAimehuyfR0rCTRLuP1Dg=";  # see note below
        };
      in {
        packages.default = self.packages.${system}.trackgbfs;

        packages.trackgbfs = pkgs.stdenv.mkDerivation (finalAttrs: {
          pname = "trackgbfs";
          version = "0.0.1";
          src = ./.;

          nativeBuildInputs = [ graalvm pkgs.gradle ];
          buildInputs = [ pkgs.zlib ];

          # Hermetic Gradle dependency resolution. Regenerate deps.json with:
          #   $(nix build .#trackgbfs.mitmCache.updateScript --print-out-paths)
          mitmCache = pkgs.gradle.fetchDeps {
            pkg = finalAttrs.finalPackage;
            data = ./deps.json;
          };

          gradleBuildTask = "nativeCompile";

          # Read by build.gradle's graalvmNative buildArgs to point the
          # native-image linker at zlib in the Nix store.
          JAVA_HOME = "${graalvm}";
          ZLIB_LIB  = "${pkgs.zlib.out}/lib";
          GRAALVM_METADATA_REPO = "${graalvmReachabilityMetadata}";

          installPhase = ''
            runHook preInstall
            install -Dm755 build/native/nativeCompile/trackgbfs $out/bin/trackgbfs
            runHook postInstall
          '';

          meta = {
            description = "GBFS v1 client and public API for archival";
            mainProgram = "trackgbfs";
            license = pkgs.lib.licenses.mit; # adjust if you've picked another
            platforms = pkgs.lib.platforms.linux;
          };
        });

        devShells.default = pkgs.mkShell {
          packages = [ graalvm pkgs.zlib pkgs.gradle ];
          JAVA_HOME = "${graalvm}";
          ZLIB_LIB  = "${pkgs.zlib.out}/lib";
          shellHook = ''
            echo "trackgbfs dev shell"
            echo "  JAVA_HOME = $JAVA_HOME"
            echo "  Run: ./gradlew bootRun        (dev server)"
            echo "  Run: ./gradlew nativeCompile  (native build)"
          '';
        };

        apps.default = {
          type = "app";
          program = "${self.packages.${system}.default}/bin/trackgbfs";
        };
      }
    ) // {
      nixosModules.default = { config, lib, pkgs, ... }:
        let cfg = config.services.trackgbfs;
        in {
          options.services.trackgbfs = {
            enable = lib.mkEnableOption "trackgbfs GBFS archiver";

            package = lib.mkOption {
              type = lib.types.package;
              default = self.packages.${pkgs.stdenv.hostPlatform.system}.default;
              defaultText = lib.literalExpression "self.packages.\${pkgs.stdenv.hostPlatform.system}.default";
              description = "trackgbfs package to run.";
            };

            port = lib.mkOption {
              type = lib.types.port;
              default = 8080;
              description = "TCP port the HTTP server listens on.";
            };

            feedBaseUrl = lib.mkOption {
              type = lib.types.str;
              example = "https://quebec.publicbikesystem.net/customer/ube/gbfs/v1";
              description = "Base URL of the upstream GBFS feed.";
            };

            feedLanguage = lib.mkOption {
              type = lib.types.str;
              default = "en";
              description = "Language code published by the feed.";
            };

            openFirewall = lib.mkOption {
              type = lib.types.bool;
              default = false;
              description = "Open the configured TCP port in the firewall.";
            };
          };

          config = lib.mkIf cfg.enable {
            networking.firewall.allowedTCPPorts =
              lib.mkIf cfg.openFirewall [ cfg.port ];

            systemd.services.trackgbfs = {
              description = "trackgbfs GBFS archiver";
              wantedBy = [ "multi-user.target" ];
              after = [ "network-online.target" ];
              wants = [ "network-online.target" ];

              # Spring Boot relaxed binding turns these into
              # server.port, trackgbfs.feed.base-url, etc.
              environment = {
                SERVER_PORT             = toString cfg.port;
                TRACKGBFS_DB_URL        = "jdbc:sqlite:%S/trackgbfs/trackgbfs.db";
                TRACKGBFS_FEED_BASE_URL = cfg.feedBaseUrl;
                TRACKGBFS_FEED_LANGUAGE = cfg.feedLanguage;
              };

              serviceConfig = {
                ExecStart = lib.getExe cfg.package;
                Restart   = "on-failure";
                RestartSec = "5s";

                DynamicUser    = true;
                StateDirectory = "trackgbfs";
                WorkingDirectory = "%S/trackgbfs";

                NoNewPrivileges       = true;
                PrivateTmp            = true;
                ProtectSystem         = "strict";
                ProtectHome           = true;
                ProtectKernelTunables = true;
                ProtectKernelModules  = true;
                ProtectControlGroups  = true;
                RestrictAddressFamilies = [ "AF_INET" "AF_INET6" "AF_UNIX" ];
                RestrictNamespaces    = true;
                LockPersonality       = true;
                RestrictRealtime      = true;
                SystemCallArchitectures = "native";
              };
            };
          };
        };
    };
}