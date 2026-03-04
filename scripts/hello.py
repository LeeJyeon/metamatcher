import os

webhook_url = os.environ.get("DETEKT_REPORT_SLACK_WEBHOOK_URL", "not set")
print(f"DETEKT_REPORT_SLACK_WEBHOOK_URL: {webhook_url}")
