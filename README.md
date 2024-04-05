# mega-backend


# mega-backend-native
- package: mvn clean package -Dquarkus.profile=dev -Dnative -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-17
- build: docker buildx build --platform linux/amd64 -f Containerfile.native -t quarkus-mega-native .
- run: docker run -i --rm -p 8080:8080 quarkus-mega-native