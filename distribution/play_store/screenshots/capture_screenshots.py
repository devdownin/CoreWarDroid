from playwright.sync_api import sync_playwright
import time

def capture_screenshots():
    with sync_playwright() as p:
        # Use a mobile-like viewport for Play Store screenshots
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={'width': 1080, 'height': 1920}, device_scale_factor=2)
        page = context.new_page()

        # Navigate to the Wasm app
        page.goto("http://localhost:8081/")

        # Wait for loading
        time.sleep(15)

        # 1. Home Screen
        page.screenshot(path="distribution/play_store/screenshots/1_home.png")

        # 2. Try to go to Battle Screen
        try:
            page.get_by_text("START BATTLE", exact=False).click()
            time.sleep(5)
            page.screenshot(path="distribution/play_store/screenshots/2_battle.png")
        except:
            print("Could not navigate to Battle")

        # 3. Try to go to Editor
        try:
            page.goto("http://localhost:8081/") # Reset
            time.sleep(5)
            page.get_by_text("EDITOR", exact=False).click()
            time.sleep(5)
            page.screenshot(path="distribution/play_store/screenshots/3_editor.png")
        except:
            print("Could not navigate to Editor")

        # 4. Try to go to Academy
        try:
            page.goto("http://localhost:8081/") # Reset
            time.sleep(5)
            page.get_by_text("ACADEMY", exact=False).click()
            time.sleep(5)
            page.screenshot(path="distribution/play_store/screenshots/4_academy.png")
        except:
            print("Could not navigate to Academy")

        browser.close()

if __name__ == "__main__":
    capture_screenshots()
