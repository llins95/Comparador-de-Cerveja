# 🍺 Cerva

**Cerva — compare e economize.**

Aplicativo Android **offline** para comparar ofertas de cerveja de forma justa pelo **preço por litro (R$/L)**.

## Objetivo

Uma lata, um pack, uma garrafa de 600 ml e um litrão podem ter preços muito diferentes. A Cerva normaliza tudo pela quantidade total de bebida:

```text
volume total = volume por unidade × quantidade
preço por litro = preço total ÷ volume total × 1000
```

Exemplo: 12 latas de 350 ml por R$ 39,90 correspondem a 4,2 L e **R$ 9,50/L**.

## Recursos

- cadastro manual de ofertas;
- quantidade e volume entram corretamente no cálculo;
- ranking automático da opção mais barata para a mais cara;
- simulador: quanto volume é possível comprar com um orçamento;
- histórico local;
- exclusão de registros;
- identificação de loja e vasilhame retornável;
- funcionamento totalmente offline com Room;
- Material Design 3 / Material You, com cores dinâmicas no Android 12+;
- ícone adaptativo da Cerva e suporte a ícone temático no Android 13+;
- arquitetura preparada para fontes de preço futuras, como uma integração oficial com o Zé Delivery.

## Atualizações automáticas

A Cerva segue o mesmo modelo de atualização do FinFlow:

- verifica a última GitHub Release ao abrir o aplicativo;
- compara a atualização pelo `versionCode`;
- oferece **Depois** ou **Baixar e instalar**;
- baixa `Cerva.apk` no cache privado do aplicativo;
- valida o arquivo usando `Cerva.apk.sha256` antes de abrir o instalador;
- usa `FileProvider`, sem expor caminhos privados;
- a confirmação final é sempre feita pelo instalador oficial do Android.

As releases de produção precisam ser assinadas sempre com o mesmo keystore. O workflow espera os Repository Secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` e `ANDROID_KEY_PASSWORD`. O keystore e suas senhas nunca devem ser versionados no repositório público.

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

🚧 Em desenvolvimento.

## Licença

MIT.
