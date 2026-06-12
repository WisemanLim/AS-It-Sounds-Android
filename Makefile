# as-it-sounds-android — android-compose (정적 템플릿 / static scaffold template)
.PHONY: dev test build deploy

dev:    ; ./gradlew installDevDebug
test:   ; ./gradlew testDevDebugUnitTest connectedDevDebugAndroidTest
build:  ; ./gradlew bundleProdRelease
deploy: ; cd fastlane && bundle exec fastlane android beta
