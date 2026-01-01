from django.db import models
from django.contrib.auth.models import User

class Currency(models.Model):
    code = models.CharField(max_length=3, unique=True)
    name = models.CharField(max_length=50)
    symbol = models.CharField(max_length=5, blank=True, null=True)
    flag_emoji = models.CharField(max_length=10, blank=True, null=True)
    
    def __str__(self):
        return f"{self.code} - {self.name}"

class ExchangeRate(models.Model):
    from_currency = models.ForeignKey(Currency, on_delete=models.CASCADE, related_name='from_rates')
    to_currency = models.ForeignKey(Currency, on_delete=models.CASCADE, related_name='to_rates')
    rate = models.DecimalField(max_digits=20, decimal_places=6)
    last_updated = models.DateTimeField(auto_now=True)
    
    class Meta:
        unique_together = ('from_currency', 'to_currency')
    
    def __str__(self):
        return f"{self.from_currency.code} to {self.to_currency.code}: {self.rate}"

class FavoriteCurrencyPair(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='favorite_currencies')
    from_currency = models.ForeignKey(Currency, on_delete=models.CASCADE, related_name='favorite_from')
    to_currency = models.ForeignKey(Currency, on_delete=models.CASCADE, related_name='favorite_to')
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        unique_together = ('user', 'from_currency', 'to_currency')
    
    def __str__(self):
        return f"{self.user.username}'s favorite: {self.from_currency.code}/{self.to_currency.code}"
