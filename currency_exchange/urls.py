from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

router = DefaultRouter()
router.register(r'currencies', views.CurrencyViewSet, basename='currency')
router.register(r'exchange-rates', views.ExchangeRateViewSet, basename='exchangerate')
router.register(r'favorites', views.FavoriteCurrencyPairViewSet, basename='favorite')

urlpatterns = [
    path('', include(router.urls)),
]
