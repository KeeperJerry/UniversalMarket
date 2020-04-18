# UniversalMarket / Shop
Магазин предметов/блоков, основаннный на MySQL и работающий на Sponge


## Команды

Все команды магазина можно посмотреть `/shop help` или `/um help`

### Открыть магазин
`/shop`, `/shop open`, `/shop o`

### Добавить предмет
`/shop add, /shop a`
`/shop add (price)`
`/shop add (price) (<optional>amount)`

### Reload Config
`/shop reload, /shop r`

## The Market
![Market](https://gyazo.com/aa747f5486fbe224f74984d94bbd91f6.png)
## Market Item
![MarketItem](https://gyazo.com/34bf241b733cbed513214f9d89bf177d.png)
## Market Config
![MarketConfig](https://gyazo.com/8620a0d03a31c549d692fa37a4540de6.png)

## Permissions

`com.xwaffle.universalmarket.open` - Разрешение на открытие магазина (общее использование).

`com.xwaffle.universalmarket.add` - Разрешение на добавление списка продажи предметов.

`com.xwaffle.universalmarket.remove` - Разрешение на удаление предмета из списка игроков.
![MarketRemove](https://gyazo.com/bb9fbd4406a8c85dd7f74e0adbeedb33.png)

#### Если опция `use-permission-to-sell` подключена как `true`

**Примечание: При включении обязательно установите `total-items-player-can-sell` опция конфигурации должна быть выше, чем самый высокий узел разрешения, который вы выдаваете.*

`com.xwaffle.universalmarket.addmax.##` - Устанавливает количество предметов, которые пользователь может продать в магазине.

Пример: `com.xwaffle.universalmarket.addmax.5`, - это позволит пользователю одновременно продавать 5 товаров в магазине.

