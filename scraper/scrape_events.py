#!/usr/bin/env python3
"""
Event Scraper for Alumni Portal
Scrapes events from university website and uploads to Firebase Firestore
"""

import time
import json
from datetime import datetime, timedelta
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import TimeoutException, NoSuchElementException
import firebase_admin
from firebase_admin import credentials, firestore

# Configuration
UNIVERSITY_EVENTS_URL = "https://www.must.ac.ug/events/"
FIREBASE_CREDENTIALS_PATH = "serviceAccountKey.json"  # Path to your Firebase service account key

class EventScraper:
    def __init__(self):
        """Initialize the scraper with Selenium and Firebase"""
        # Setup Chrome options
        chrome_options = Options()
        chrome_options.add_argument('--headless')  # Run in background
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--window-size=1920,1080')
        
        # Initialize Chrome driver
        self.driver = webdriver.Chrome(options=chrome_options)
        self.wait = WebDriverWait(self.driver, 10)
        
        # Initialize Firebase
        try:
            cred = credentials.Certificate(FIREBASE_CREDENTIALS_PATH)
            firebase_admin.initialize_app(cred)
            self.db = firestore.client()
            print("✓ Firebase initialized successfully")
        except Exception as e:
            print(f"✗ Firebase initialization failed: {e}")
            raise
    
    def scrape_events(self):
        """Scrape events from the university website"""
        print(f"\n🔍 Scraping events from: {UNIVERSITY_EVENTS_URL}")
        
        try:
            self.driver.get(UNIVERSITY_EVENTS_URL)
            time.sleep(3)  # Wait for page to load
            
            events = []
            
            # MUST website uses Elementor loop items
            event_elements = self.driver.find_elements(By.CSS_SELECTOR, 'div.e-loop-item')
            
            print(f"Found {len(event_elements)} event elements")
            
            for idx, element in enumerate(event_elements, 1):
                try:
                    # Extract event data from MUST website structure
                    # Title is in h2.elementor-heading-title > a
                    title_element = element.find_element(By.CSS_SELECTOR, 'h2.elementor-heading-title a')
                    title = title_element.text.strip()
                    event_url = title_element.get_attribute('href')
                    
                    # Description is in div.elementor-widget-text-editor
                    try:
                        description = element.find_element(By.CSS_SELECTOR, 'div.elementor-widget-text-editor .elementor-widget-container').text.strip()
                    except:
                        description = title  # Fallback to title
                    
                    # Extract date from the date box (month, day, year)
                    try:
                        month = element.find_elements(By.CSS_SELECTOR, 'div.elementor-heading-title')[0].text.strip()
                        day = element.find_elements(By.CSS_SELECTOR, 'h2.elementor-heading-title')[0].text.strip()
                        year = element.find_elements(By.CSS_SELECTOR, 'h2.elementor-heading-title')[1].text.strip()
                        date_str = f"{month} {day}, {year}"
                    except:
                        date_str = ""
                    
                    # Extract time from icon list
                    time_str = ""
                    try:
                        time_element = element.find_element(By.CSS_SELECTOR, 'li.elementor-icon-list-item .elementor-icon-list-text')
                        time_str = time_element.text.strip()
                    except:
                        time_str = "10:00 AM"  # Default time
                    
                    # Extract venue from icon list (second item)
                    venue = "MUST Campus"  # Default venue
                    try:
                        venue_elements = element.find_elements(By.CSS_SELECTOR, 'li.elementor-icon-list-item .elementor-icon-list-text')
                        if len(venue_elements) > 1:
                            venue = venue_elements[1].text.strip()
                    except:
                        pass
                    
                    # Try to get image (if any)
                    image_url = ""
                    try:
                        image_url = element.find_element(By.CSS_SELECTOR, 'img').get_attribute('src')
                    except:
                        pass
                    
                    # Event type - extract from classes or default
                    event_type = "University Event"
                    try:
                        class_attr = element.get_attribute('class')
                        if 'campus-event' in class_attr:
                            event_type = "Campus Event"
                        elif 'conference-symposium' in class_attr:
                            event_type = "Conference"
                        elif 'research-dissemination' in class_attr:
                            event_type = "Research Conference"
                        elif 'lectures-seminars' in class_attr:
                            event_type = "Seminar"
                    except:
                        pass
                    
                    # Parse date with time
                    start_timestamp = self.parse_date(date_str, time_str)
                    end_timestamp = start_timestamp + (3 * 60 * 60 * 1000)  # Default 3 hours duration
                    
                    event = {
                        'title': title,
                        'description': description[:500] if len(description) > 500 else description,
                        'venue': venue,
                        'address': venue,  # Use venue as address
                        'imageUrl': image_url,
                        'eventUrl': event_url,
                        'startDateTime': start_timestamp,
                        'endDateTime': end_timestamp,
                        'eventType': event_type,
                        'isPublic': True,
                        'organizer': 'MUST',
                        'organizerName': 'Mbarara University of Science and Technology',
                        'organizerContact': 'events@must.ac.ug',
                        'registrationUrl': event_url,
                        'isOnline': False,
                        'onlineLink': '',
                        'requiresRegistration': True,
                        'isFree': True,
                        'ticketPrice': '',
                        'maxAttendees': 100,
                        'currentAttendees': 0,
                        'attendeeIds': [],
                        'tags': [event_type, 'MUST', 'University Event'],
                        'targetAudience': 'all',
                        'targetGraduationYears': [],
                        'isFeatured': False,
                        'createdAt': firestore.SERVER_TIMESTAMP,
                        'updatedAt': firestore.SERVER_TIMESTAMP,
                        'scrapedAt': firestore.SERVER_TIMESTAMP,
                        'source': 'must_website',
                        'registrationDeadline': start_timestamp - (24 * 60 * 60 * 1000),
                        'additionalDetails': {
                            'eventUrl': event_url,
                            'source': 'must_website'
                        }
                    }
                    
                    events.append(event)
                    print(f"  {idx}. {title} - {date_str}")
                    
                except Exception as e:
                    print(f"  ✗ Error parsing event {idx}: {e}")
                    continue
            
            return events
            
        except Exception as e:
            print(f"✗ Error scraping events: {e}")
            return []
    
    def parse_date(self, date_str, time_str="10:00 AM"):
        """Parse date string to timestamp (milliseconds)"""
        try:
            if not date_str:
                # Default to 30 days from now
                future_date = datetime.now() + timedelta(days=30)
                return int(future_date.timestamp() * 1000)
            
            # Try different date formats used by MUST website
            formats = [
                "%B %d, %Y",            # November 11, 2025
                "%b %d, %Y",            # Nov 11, 2025
                "%d %B %Y",             # 11 November 2025
                "%d %b %Y",             # 11 Nov 2025
                "%Y-%m-%d",             # 2025-11-11
                "%d/%m/%Y",             # 11/11/2025
                "%d-%m-%Y",             # 11-11-2025
            ]
            
            # Clean the date string
            date_str = date_str.strip()
            
            for fmt in formats:
                try:
                    dt = datetime.strptime(date_str, fmt)
                    # Parse time if provided
                    if time_str and time_str != "10:00 AM":
                        try:
                            # Parse time like "2:00 PM" or "9:00 AM"
                            time_obj = datetime.strptime(time_str, "%I:%M %p").time()
                            dt = dt.replace(hour=time_obj.hour, minute=time_obj.minute)
                        except:
                            # If time parsing fails, use 10:00 AM
                            dt = dt.replace(hour=10, minute=0)
                    else:
                        # Default to 10:00 AM
                        dt = dt.replace(hour=10, minute=0)
                    return int(dt.timestamp() * 1000)
                except ValueError:
                    continue
            
            # If no format matches, return date 30 days from now
            print(f"  ⚠ Could not parse date '{date_str}', using default")
            future_date = datetime.now() + timedelta(days=30)
            return int(future_date.timestamp() * 1000)
            
        except Exception as e:
            print(f"  ⚠ Date parsing error for '{date_str}': {e}")
            future_date = datetime.now() + timedelta(days=30)
            return int(future_date.timestamp() * 1000)
    
    def upload_to_firestore(self, events):
        """Upload scraped events to Firestore"""
        print(f"\n📤 Uploading {len(events)} events to Firestore...")
        
        uploaded = 0
        skipped = 0
        
        for event in events:
            try:
                # Create unique ID based on title and date to avoid duplicates
                event_id = f"scraped_{event['title'].lower().replace(' ', '_')}_{event['startDateTime']}"
                event_id = event_id[:100]  # Limit ID length
                
                # Check if event already exists
                doc_ref = self.db.collection('events').document(event_id)
                doc = doc_ref.get()
                
                if doc.exists:
                    print(f"  ⊘ Skipped (already exists): {event['title']}")
                    skipped += 1
                else:
                    doc_ref.set(event)
                    print(f"  ✓ Uploaded: {event['title']}")
                    uploaded += 1
                    
            except Exception as e:
                print(f"  ✗ Error uploading '{event['title']}': {e}")
        
        print(f"\n✅ Upload complete: {uploaded} uploaded, {skipped} skipped")
        return uploaded, skipped
    
    def run(self):
        """Main execution method"""
        try:
            print("=" * 60)
            print("🎓 Alumni Portal Event Scraper")
            print("=" * 60)
            
            # Scrape events
            events = self.scrape_events()
            
            if not events:
                print("\n⚠ No events found. Check your selectors!")
                return
            
            # Upload to Firestore
            uploaded, skipped = self.upload_to_firestore(events)
            
            print("\n" + "=" * 60)
            print(f"✅ Scraping completed successfully!")
            print(f"   Total events found: {len(events)}")
            print(f"   Uploaded: {uploaded}")
            print(f"   Skipped: {skipped}")
            print("=" * 60)
            
        except Exception as e:
            print(f"\n✗ Fatal error: {e}")
            raise
        finally:
            self.driver.quit()
            print("\n🔒 Browser closed")

def main():
    """Entry point"""
    scraper = EventScraper()
    scraper.run()

if __name__ == "__main__":
    main()
