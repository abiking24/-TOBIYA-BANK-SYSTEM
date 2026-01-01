from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from django.db.models import Q
from .models import Currency, ExchangeRate, FavoriteCurrencyPair
from .serializers import (
    CurrencySerializer, 
    ExchangeRateSerializer,
    FavoriteCurrencyPairSerializer,
    ConvertCurrencySerializer,
    HistoricalRateSerializer
)
from datetime import datetime, timedelta
import requests
from django.utils import timezone

class CurrencyViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Currency.objects.all()
    serializer_class = CurrencySerializer
    
    def get_queryset(self):
        queryset = super().get_queryset()
        search = self.request.query_params.get('search', None)
        if search:
            queryset = queryset.filter(
                Q(code__icontains=search) | 
                Q(name__icontains=search)
            )
        return queryset

class ExchangeRateViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = ExchangeRateSerializer
    
    def get_queryset(self):
        queryset = ExchangeRate.objects.select_related('from_currency', 'to_currency')
        
        from_currency = self.request.query_params.get('from_currency')
        to_currency = self.request.query_params.get('to_currency')
        
        if from_currency:
            queryset = queryset.filter(from_currency__code=from_currency.upper())
        if to_currency:
            queryset = queryset.filter(to_currency__code=to_currency.upper())
            
        return queryset
    
    @action(detail=False, methods=['post'])
    def convert(self, request):
        serializer = ConvertCurrencySerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        from_currency = serializer.validated_data['from_currency']
        to_currency = serializer.validated_data['to_currency']
        amount = serializer.validated_data['amount']
        
        try:
            rate = ExchangeRate.objects.get(
                from_currency__code=from_currency,
                to_currency__code=to_currency
            )
            converted_amount = float(amount) * float(rate.rate)
            
            return Response({
                'from_currency': from_currency,
                'to_currency': to_currency,
                'amount': amount,
                'converted_amount': round(converted_amount, 6),
                'rate': rate.rate,
                'last_updated': rate.last_updated
            })
            
        except ExchangeRate.DoesNotExist:
            return Response(
                {'error': 'Exchange rate not found'}, 
                status=status.HTTP_404_NOT_FOUND
            )
    
    @action(detail=False, methods=['get'])
    def historical(self, request):
        serializer = HistoricalRateSerializer(data=request.query_params)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        from_currency = serializer.validated_data['from_currency']
        to_currency = serializer.validated_data['to_currency']
        days = serializer.validated_data.get('days', 7)
        
        # In a real application, you would fetch historical data from your database
        # or an external API. This is a simplified example.
        end_date = timezone.now()
        start_date = end_date - timedelta(days=days)
        
        # Mock historical data - replace with actual data fetching logic
        historical_data = []
        for i in range(days + 1):
            date = start_date + timedelta(days=i)
            # In a real app, fetch the actual rate for each date
            historical_data.append({
                'date': date.date(),
                'rate': 1.0 + (i * 0.01)  # Mock rate
            })
        
        return Response({
            'from_currency': from_currency,
            'to_currency': to_currency,
            'historical_data': historical_data
        })

class FavoriteCurrencyPairViewSet(viewsets.ModelViewSet):
    serializer_class = FavoriteCurrencyPairSerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        return FavoriteCurrencyPair.objects.filter(user=self.request.user)
    
    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
    
    @action(detail=False, methods=['get'])
    def check_favorite(self, request):
        from_currency = request.query_params.get('from_currency')
        to_currency = request.query_params.get('to_currency')
        
        if not from_currency or not to_currency:
            return Response(
                {'error': 'Both from_currency and to_currency parameters are required'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        is_favorite = FavoriteCurrencyPair.objects.filter(
            user=request.user,
            from_currency__code=from_currency.upper(),
            to_currency__code=to_currency.upper()
        ).exists()
        
        return Response({'is_favorite': is_favorite})
