from rest_framework import serializers
from .models import Currency, ExchangeRate, FavoriteCurrencyPair

class CurrencySerializer(serializers.ModelSerializer):
    class Meta:
        model = Currency
        fields = ['id', 'code', 'name', 'symbol', 'flag_emoji']

class ExchangeRateSerializer(serializers.ModelSerializer):
    from_currency = CurrencySerializer()
    to_currency = CurrencySerializer()
    
    class Meta:
        model = ExchangeRate
        fields = ['id', 'from_currency', 'to_currency', 'rate', 'last_updated']

class ConvertCurrencySerializer(serializers.Serializer):
    from_currency = serializers.CharField(max_length=3)
    to_currency = serializers.CharField(max_length=3)
    amount = serializers.DecimalField(max_digits=20, decimal_places=6)
    
    def validate_from_currency(self, value):
        return value.upper()
    
    def validate_to_currency(self, value):
        return value.upper()

class HistoricalRateSerializer(serializers.Serializer):
    from_currency = serializers.CharField(max_length=3)
    to_currency = serializers.CharField(max_length=3)
    days = serializers.IntegerField(min_value=1, max_value=365, default=7)
    
    def validate_from_currency(self, value):
        return value.upper()
    
    def validate_to_currency(self, value):
        return value.upper()

class FavoriteCurrencyPairSerializer(serializers.ModelSerializer):
    from_currency = CurrencySerializer(read_only=True)
    to_currency = CurrencySerializer(read_only=True)
    from_currency_code = serializers.CharField(write_only=True)
    to_currency_code = serializers.CharField(write_only=True)
    
    class Meta:
        model = FavoriteCurrencyPair
        fields = [
            'id', 'from_currency', 'to_currency', 
            'from_currency_code', 'to_currency_code', 'created_at'
        ]
        read_only_fields = ['id', 'created_at', 'user']
    
    def create(self, validated_data):
        from_currency_code = validated_data.pop('from_currency_code')
        to_currency_code = validated_data.pop('to_currency_code')
        
        try:
            from_currency = Currency.objects.get(code=from_currency_code.upper())
            to_currency = Currency.objects.get(code=to_currency_code.upper())
        except Currency.DoesNotExist:
            raise serializers.ValidationError("One or both currency codes are invalid")
        
        return FavoriteCurrencyPair.objects.create(
            user=self.context['request'].user,
            from_currency=from_currency,
            to_currency=to_currency
        )
