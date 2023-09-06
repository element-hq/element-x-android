# Fix this error:
# ERROR: Missing classes detected while running R8. Please add the missing classes or apply additional keep rules that are generated in /Users/bmarty/workspaces/element-x-android/app/build/outputs/mapping/nightly/missing_rules.txt.
# ERROR: R8: Missing class com.google.firebase.analytics.connector.AnalyticsConnector (referenced from: void com.google.firebase.messaging.MessagingAnalytics.logToScion(java.lang.String, android.os.Bundle) and 1 other context)
-dontwarn com.google.firebase.analytics.connector.AnalyticsConnector
