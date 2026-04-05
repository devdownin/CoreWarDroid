from playwright.sync_api import sync_playwright
import os

def generate_assets():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        # Open the local HTML file
        file_path = os.path.abspath("distribution/play_store/assets/assets_generator.html")
        page.goto(f"file://{file_path}")

        # Take a screenshot of the #icon element (512x512)
        icon_element = page.query_selector("#icon")
        icon_element.screenshot(path="distribution/play_store/assets/play_store_512.png")

        # Take a screenshot of the #feature element (1024x500)
        feature_element = page.query_selector("#feature")
        feature_element.screenshot(path="distribution/play_store/assets/feature_graphic_1024x500.png")

        browser.close()

if __name__ == "__main__":
    generate_assets()
