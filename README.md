# Bright

Bright, Minecraft 1.20.1 Fabric icin hazirlanmis hafif bir istemci modudur.
Sadece 3 modul icerir, hepsinin kendi ayar paneli vardir:

- **Hitbox** — hedef varliklarin carpisma boyutunu XZ/Y eksenlerinde buyutur.
- **Trigger** — nisangah bir hedefin uzerindeyken otomatik vurur (gecikme,
  kalkan/yemek engelleme ayarlari ile).
- **ESP** — canli varliklarin etrafina renkli tel kafes kutu cizer; "Duvar
  Icinden Gor" acikken engellerin arkasindan da gorunur.

## Ayar ekrani

Menu **Sag Shift** tusuyla acilir (tus Kontroller > Bright altindan
degistirilebilir). Mod ModMenu'ye bagli degildir ve ModMenu listesinde
varsayilan olarak gorunmez (`library` rozeti); yalnizca ModMenu ayarlarinda
"Kutuphaneleri goster" acilirsa listelenir. Ekrandaki
her modulun solundaki satira tiklamak acar/kapatir, sagdaki dislisi o
modulun ayar panelini gosterir. Ayarlar `config/bright.json` dosyasinda
otomatik olarak kaydedilir.

## Derleme

```
./gradlew build
```

Cikti `build/libs/bright-1.0.0.jar` altinda olusur.

> Not: Bu proje 1.21.x hedefli onceki bir surumden 1.20.1'e tasindi
> (Yarn/Fabric API surumleri ve bazi render cagrilari degisti — ozellikle
> `RenderSystem.setShader` ve `Tessellator` kullanimlari). Bu ortamda
> internet erisimi olmadigi icin Minecraft/Yarn kutuphaneleri indirilip
> gercek bir Gradle derlemesi yapilamadi; ilk derlemede kucuk bir mapping
> uyusmazligi cikarsa (ornegin `tessellator.draw()` yerine baska bir isim
> beklenmesi gibi) hatanin gosterdigi satiri duzeltmeniz yeterli olur.


## Duzeltme notlari

- **ESP:** `WorldRenderEvents.LAST` icinde el ile `POSITION_COLOR` + `LINES`
  cizimi 1.20.1'de gorunmuyordu. Simdi `AFTER_TRANSLUCENT` asamasinda vanilla
  `WorldRenderer.drawBox` + `RenderLayer.getLines()` kullaniliyor. Kutu tick
  interpolasyonu ile cizilir. Not: cizgi kalinligi vanilla `getLines()` katmani
  tarafindan belirlendigi icin "Cizgi Kalinligi" ayari bu surumde etkisizdir.
- **Hitbox:** Elytra + havai fisek ile ucarken poz `FALL_FLYING` olur ve vanilla
  kutu 0.6 x 0.6'ya kuculur; eski kod bu kucuk kutuyu carptigi icin hitbox
  normale donuyordu. Simdi buyutme her zaman ayakta durma boyutuna gore ve
  kutunun gercek merkezi etrafinda yapilir.
- ModMenu bagimliligi tamamen kaldirildi (`build.gradle`, `fabric.mod.json`, `ModMenuIntegration`).
- Ikon yenilendi: isik / ampul temali.
- Dil dosyalari yanlis klasordeydi (`resources/resources/assets`), tasindi.
