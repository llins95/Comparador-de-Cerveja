# 🍺 Cerva

Aplicativo Android **offline** para comparar ofertas de cerveja de forma justa pelo **preço por litro (R$/L)**.

## Objetivo

Uma lata, um pack, uma garrafa de 600 ml e um litrão podem ter preços muito diferentes. A **Cerva** normaliza tudo pela quantidade total de bebida:

```text
volume total = volume por unidade × quantidade
preço por litro = preço total ÷ volume total × 1000
```

Exemplo: 12 latas de 350 ml por R$ 39,90 correspondem a 4,2 L e **R$ 9,50/L**.

## Primeira versão

- cadastro manual de ofertas;
- quantidade e volume entram corretamente no cálculo;
- ranking automático da opção mais barata para a mais cara;
- simulador: quanto volume é possível comprar com um orçamento;
- histórico local;
- exclusão de registros;
- identificação de loja e vasilhame retornável;
- funcionamento totalmente offline com Room;
- interface em Material Design 3 com cores dinâmicas no Android 12+;
- ícone adaptativo com suporte a ícone temático no Android 13+;
- arquitetura preparada para fontes de preço futuras (ex.: integração oficial com Zé Delivery).

## Tecnologias

- Kotlin
- Jetpack Compose + Material 3
- Room
- MVVM
- Flow / StateFlow
- Gradle / GitHub Actions

## Requisitos de desenvolvimento

- Android Studio compatível com Android Gradle Plugin 9.3.x
- JDK 17 ou superior
- Android SDK 37

## Status

🚧 Em desenvolvimento. A versão inicial tem como foco validar a experiência offline e as regras de comparação.

## Licença

MIT.
