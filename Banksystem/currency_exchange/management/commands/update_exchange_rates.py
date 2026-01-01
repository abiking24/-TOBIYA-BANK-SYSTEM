import json
import requests
from django.core.management.base import BaseCommand
from django.conf import settings
from ...models import Currency, ExchangeRate
from datetime import datetime

class Command(BaseCommand):
    help = 'Fetches and updates exchange rates from an external API'
    
    def handle(self, *args, **options):
        # Get API key from settings (you should add this to your settings.py)
        api_key = getattr(settings, 'EXCHANGE_RATE_API_KEY', None)
        if not api_key:
            self.stderr.write('EXCHANGE_RATE_API_KEY not found in settings')
            return
        
        # Base currency (USD)
        base_currency = 'USD'
        
        # Get or create base currency
        base_currency_obj, _ = Currency.objects.get_or_create(
            code=base_currency,
            defaults={
                'name': 'US Dollar',
                'symbol': '$',
                'flag_emoji': '🇺🇸'
            }
        )
        
        # Example API endpoint (using exchangerate-api.com as an example)
        url = f'https://v6.exchangerate-api.com/v6/{api_key}/latest/{base_currency}'
        
        try:
            response = requests.get(url)
            response.raise_for_status()
            data = response.json()
            
            if data.get('result') != 'success':
                self.stderr.write(f"API Error: {data.get('error-type', 'Unknown error')}")
                return
            
            rates = data.get('conversion_rates', {})
            
            # Get all existing currencies
            existing_currencies = {c.code: c for c in Currency.objects.all()}
            
            # Update or create exchange rates
            for currency_code, rate in rates.items():
                if currency_code == base_currency:
                    continue
                    
                # Get or create the target currency
                if currency_code not in existing_currencies:
                    # You might want to add more currency details here
                    currency_obj = Currency.objects.create(
                        code=currency_code,
                        name=currency_code,  # You might want to add a proper name
                        symbol=currency_code  # You might want to add a proper symbol
                    )
                    existing_currencies[currency_code] = currency_obj
                else:
                    currency_obj = existing_currencies[currency_code]
                
                # Update or create exchange rate
                ExchangeRate.objects.update_or_create(
                    from_currency=base_currency_obj,
                    to_currency=currency_obj,
                    defaults={'rate': rate}
                )
            
            self.stdout.write(self.style.SUCCESS(f'Successfully updated {len(rates)} exchange rates'))
            
        except requests.RequestException as e:
            self.stderr.write(f'Error fetching exchange rates: {str(e)}')
