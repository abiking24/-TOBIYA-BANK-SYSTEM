from django.contrib import admin
from .models import Currency, ExchangeRate, FavoriteCurrencyPair

@admin.register(Currency)
class CurrencyAdmin(admin.ModelAdmin):
    list_display = ('code', 'name', 'symbol', 'flag_emoji')
    search_fields = ('code', 'name')
    ordering = ('code',)

@admin.register(ExchangeRate)
class ExchangeRateAdmin(admin.ModelAdmin):
    list_display = ('from_currency', 'to_currency', 'rate', 'last_updated')
    list_filter = ('from_currency', 'to_currency')
    search_fields = ('from_currency__code', 'to_currency__code')
    readonly_fields = ('last_updated',)

@admin.register(FavoriteCurrencyPair)
class FavoriteCurrencyPairAdmin(admin.ModelAdmin):
    list_display = ('user', 'from_currency', 'to_currency', 'created_at')
    list_filter = ('user', 'created_at')
    search_fields = ('user__username', 'from_currency__code', 'to_currency__code')
    readonly_fields = ('created_at',)
