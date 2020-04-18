package com.xwaffle.universalmarket.commands;

import com.xwaffle.universalmarket.UniversalMarket;
import com.xwaffle.universalmarket.market.MarketItem;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

/**
 * Created by Chase(Xwaffle) on 12/18/2017.
 */
public class MarketCommand extends BasicCommand {
    public MarketCommand() {
        super("", "Основная команда для магазина.", "/shop");
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.split(" ");

        Player player = null;
        if (source instanceof Player) {
            player = (Player) source;
        }

        long expireTime = UniversalMarket.getInstance().getMarket().getExpireTime();
        long totalListings = UniversalMarket.getInstance().getMarket().getTotalItemsCanSell();


        if (arguments.isEmpty() || arguments.equalsIgnoreCase("")) {

            if (player != null) {
                if (player.hasPermission("com.xwaffle.universalmarket.open")) {
                    UniversalMarket.getInstance().getMarket().openMarket(player);
                } else {
                    source.sendMessage(Text.of(TextColors.RED, "У вас нет разрешения на просмотр магазина."));
                }
            } else {
                source.sendMessage(Text.of(TextColors.RED + "Вы не можете открыть рынок из консоли!"));
            }
            return CommandResult.success();
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "open":
                case "o":
                    if (player == null)
                        break;
                    if (player.hasPermission("com.xwaffle.universalmarket.open")) {
                        UniversalMarket.getInstance().getMarket().openMarket(player);
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "У вас нет разрешения на просмотр магазина."));
                    }
                    break;
                case "add":
                case "a":
                    if (player == null)
                        break;
                    if (!player.hasPermission("com.xwaffle.universalmarket.add")) {
                        player.sendMessage(Text.of(TextColors.RED, "У вас нет разрешения на добавление товаров в магазин."));
                        return CommandResult.success();
                    }

                    int listingCount = UniversalMarket.getInstance().getMarket().countListings(player.getUniqueId());
                    if (args.length < 2) {
                        player.sendMessage(Text.of(TextColors.RED, "Неверная команда!"));
                        player.sendMessage(Text.of(TextColors.YELLOW, "Используйте /shop " + args[0].toLowerCase() + " (цена предмета в руке) (<optional> количество)"));
                        return CommandResult.success();
                    }

                    if (listingCount >= totalListings) {
                        player.sendMessage(Text.of(TextColors.RED, "Вы уже продаете максимальное количество товаров за раз."));
                        return CommandResult.success();
                    }


                    if (UniversalMarket.getInstance().getMarket().isUsePermissionToSell()) {
                        int userMaxSellPerm = 0;
                        for (int i = 1; i < 99; i++) {
                            if (player.hasPermission("com.xwaffle.universalmarket.addmax." + i)) {
                                userMaxSellPerm = i;
                            }
                        }


                        if (userMaxSellPerm <= listingCount) {
                            player.sendMessage(Text.of(TextColors.RED, "Вы достигли максимального количества предметов, которые вы можете продать в магазине."));
                            player.sendMessage(Text.of(TextColors.RED, "У вас есть только разрешение на продажу ", TextColors.GRAY, userMaxSellPerm, TextColors.RED, " предметов в магазине."));
                            return CommandResult.success();
                        }

                    }


                    if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                        ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
                        double price;
                        try {
                            price = Double.parseDouble(args[1]);

                            if (price < 0) {
                                player.sendMessage(Text.of(TextColors.RED, "Вы должны ввести положительную цену!"));
                                return CommandResult.success();

                            }
                        } catch (Exception exc) {
                            player.sendMessage(Text.of(TextColors.RED, "Неверная цена за товар!"));
                            player.sendMessage(Text.of(TextColors.YELLOW, "Используйте /shop " + args[0].toLowerCase() + " (цена предмета в руке) (<optional> количество)"));
                            return CommandResult.success();
                        }

                        int amount = stack.getQuantity();

                        if (args.length >= 3) {
                            try {
                                amount = Integer.parseInt(args[2]);
                                if (amount <= 0) {
                                    player.sendMessage(Text.of(TextColors.RED, "Вы должны ввести положительное число, чтобы продать в магазине!"));
                                    return CommandResult.success();
                                } else if (amount > stack.getQuantity()) {
                                    player.sendMessage(Text.of(TextColors.RED, "Вы не можете продать больше, чем у вас есть."));
                                    return CommandResult.success();
                                }
                            } catch (Exception exc) {
                                player.sendMessage(Text.of(TextColors.RED, "Неверная сумма за товар!"));
                                player.sendMessage(Text.of(TextColors.YELLOW, "Используйте /shop " + args[0].toLowerCase() + " (цена предмета в руке) (<optional> количество)"));
                                return CommandResult.success();
                            }
                        }

                        if (UniversalMarket.getInstance().getMarket().useTax()) {
                            double tax = price * UniversalMarket.getInstance().getMarket().getTax();

                            if (UniversalMarket.getInstance().getEconomyService() == null) {
                                source.sendMessage(Text.of(TextColors.RED, "Этот сервер не использует плагин валюты! Это необходимо для использования магазина!"));
                                return CommandResult.success();
                            }

                            UniqueAccount account = UniversalMarket.getInstance().getEconomyService().getOrCreateAccount(player.getUniqueId()).get();
                            Currency currency = UniversalMarket.getInstance().getEconomyService().getDefaultCurrency();
                            if (account.getBalance(currency).doubleValue() < tax) {
                                player.sendMessage(Text.of(TextColors.RED, "Вы не можете позволить себе пункт налога!"));
                                player.sendMessage(Text.of(TextColors.RED, "You must pay ", TextColors.YELLOW, UniversalMarket.getInstance().getMarket().getTax(), TextColors.RED, " of the item price."));
                                player.sendMessage(Text.of(TextColors.RED, "Вам нужно заплатить ", TextColors.GREEN, tax, TextColors.RED, " для того, чтобы продать этот предмет на рынке."));
                                return CommandResult.success();
                            } else {
                                account.withdraw(currency, new BigDecimal(tax), Cause.of(EventContext.empty(), UniversalMarket.getInstance()));
                                player.sendMessage(Text.of(TextColors.RED, "Налог с продажи товара был взят с вас!"));
                                player.sendMessage(Text.of(TextColors.DARK_RED, "- $", TextColors.RED, tax));
                            }
                        }

                        if (UniversalMarket.getInstance().getMarket().payFlatPrice()) {
                            double flatPrice = UniversalMarket.getInstance().getMarket().getFlatPrice();
                            UniqueAccount account = UniversalMarket.getInstance().getEconomyService().getOrCreateAccount(player.getUniqueId()).get();
                            Currency currency = UniversalMarket.getInstance().getEconomyService().getDefaultCurrency();
                            if (account.getBalance(currency).doubleValue() < flatPrice) {
                                player.sendMessage(Text.of(TextColors.RED, "Вы должны заплатить ", TextColors.GRAY, "$" + flatPrice, TextColors.RED, " чтобы продать на рынке."));
                                return CommandResult.success();
                            } else {
                                account.withdraw(currency, new BigDecimal(flatPrice), Cause.of(EventContext.empty(), UniversalMarket.getInstance()));
                                player.sendMessage(Text.of(TextColors.RED, "A Market fee has been taken out!"));
                                player.sendMessage(Text.of(TextColors.DARK_RED, "- $", TextColors.RED, flatPrice));
                            }
                        }

                        if (UniversalMarket.getInstance().getMarket().isItemBlacklisted(stack.getType())) {
                            player.sendMessage(Text.of(TextColors.RED, "Этот предмет не может быть продан (" + stack.getType().getId() + ")"));
                            return CommandResult.success();
                        }


                        int prevAmount = stack.getQuantity();

                        if (amount == stack.getQuantity()) {
                            player.setItemInHand(HandTypes.MAIN_HAND, null);
                        } else {

                            stack.setQuantity(amount);
                        }


                        int id = UniversalMarket.getInstance().getDatabase().createEntry(stack.copy(), player.getUniqueId(), player.getName(), price, System.currentTimeMillis() + expireTime);
                        UniversalMarket.getInstance().getMarket().addItem(new MarketItem(id, stack.copy(), player.getUniqueId(), player.getName(), price, (System.currentTimeMillis() + expireTime)), false);
                        player.sendMessage(Text.of(TextColors.YELLOW, "Товар добавлен в ", TextColors.GRAY, "магазин", TextColors.YELLOW, " за $", TextColors.DARK_AQUA, price));

                        if (amount != prevAmount) {
                            stack.setQuantity(prevAmount - amount);
                            player.setItemInHand(HandTypes.MAIN_HAND, stack);
                        }


                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "Поместите предмет в руку, чтобы продать!"));
                    }
                    break;
                case "help":
                case "h":
                case "?":
                    source.sendMessage(Text.of(TextColors.DARK_AQUA, "==== ", TextColors.AQUA, "Помощь по магазину", TextColors.DARK_AQUA, " ===="));
                    source.sendMessage(Text.of(TextColors.YELLOW, "* /shop or /universalmarket"));
                    source.sendMessage(Text.of(TextColors.YELLOW, "* /shop add (цена) (<optional> кол-во)", TextColors.GRAY, " - ", TextColors.GREEN, "Продает текущий предмет за указанную цену."));
                    source.sendMessage(Text.of(TextColors.YELLOW, "* /shop open", TextColors.GRAY, " - ", TextColors.GREEN, "Открыть магазин."));
                    source.sendMessage(Text.of(TextColors.YELLOW, "* /shop info", TextColors.GRAY, " - ", TextColors.GREEN, "Показать текущую конфигурацию."));
                    source.sendMessage(Text.of(TextColors.YELLOW, "* /shop reload", TextColors.GRAY, " - ", TextColors.GREEN, "Перезагрузить конфиг магазина."));
                    break;
                case "reload":
                case "r":
                    if (source.hasPermission("com.xwaffle.universalmarket.reload")) {
                        UniversalMarket.getInstance().getMarket().reloadConfig();
                        source.sendMessage(Text.of(TextColors.GREEN, "Конфиг магазина был перезагружен!"));
                    } else {
                        source.sendMessage(Text.of(TextColors.RED, "У вас нет доступа к данной команде!"));
                    }
                    break;
                case "info":
                case "i":
                    source.sendMessage(Text.of(TextColors.DARK_AQUA, "Текущий процент налога: ", TextColors.AQUA, UniversalMarket.getInstance().getMarket().getTax()));
                    break;
            }
        } else {
            if (player != null) {
                UniversalMarket.getInstance().getMarket().openMarket(player);
            }
        }

        return CommandResult.success();
    }
}
