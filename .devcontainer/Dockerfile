# Start from the official Scala + sbt image (includes Java 17, sbt, Scala 3.8.2).
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.15_6_1.12.8_3.8.2

# Switch to root so we can install packages and change permissions.
USER root

# Install sudo, allow sbtuser to run commands as root, create /workspaces for the dev container,
# then clean up apt cache to keep the image small.
RUN apt-get update && apt-get install -y --no-install-recommends sudo \
  && echo "sbtuser ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/sbtuser \
  && chmod 0440 /etc/sudoers.d/sbtuser \
  && mkdir -p /workspaces && chown sbtuser:sbtuser /workspaces \
  && rm -rf /var/lib/apt/lists/*

# Run as sbtuser (non-root) for normal development.
USER sbtuser

# Your project files will be mounted here when the dev container starts.
WORKDIR /workspaces
