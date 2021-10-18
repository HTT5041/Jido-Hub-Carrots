import com.hazion.api.Blocks;
import com.hazion.api.Game;
import com.hazion.api.Inventory;
import com.hazion.api.hypixel.skyblock.SkyBlock;
import com.hazion.api.hypixel.skyblock.SkyBlockBazaar;
import com.hazion.api.input.Camera;
import com.hazion.api.input.Input;
import com.hazion.api.pathing.Movement;
import com.hazion.api.peer.network.chat.Component;
import com.hazion.api.peer.world.level.block.state.BlockState;
import com.hazion.api.script.Script;
import com.hazion.api.script.Manifest;
import com.hazion.api.utils.PlayerHelper;
import com.hazion.api.world.blocks.BlockPos;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Manifest(name = "Hub Carrots", author = "HTT5041", version = 1.1, description = "Farm da carrot in da Hub")
public class HubCarrots implements Script {

    BlockPos farm1 = new BlockPos(-4, 69, 8);
    BlockPos farm2 = new BlockPos(-6, 69, 22);
    BlockPos farm3 = new BlockPos(-16, 69, 22);
    BlockPos farm4 = new BlockPos(-22, 70, 33);
    BlockPos farm5 = new BlockPos(-41, 68, 32);
    BlockPos bazaar = new BlockPos(-33, 70, -77);

    BlockPos[] farms = {farm1, farm2, farm3, farm4, farm5};
    int currentFarm = 0;

    int counter = 0;

    boolean sellByQuickSell = false;
    boolean sellByOrder = false;
    boolean canStart = false;

    boolean shouldCollect = false;
    boolean failedClaim = false;
    boolean doFailedClaim = false;

    @Override
    public void onStart() {
        JFrame frame = new JFrame("Hub Carrots");
        frame.setSize(200,130);

        JLabel label = new JLabel("Select sell method:");
        label.setBounds(0,0,185,20);

        JLabel label1 = new JLabel("Made with <3 by HTT5041");
        label1.setBounds(25,65,185,20);

        String[] options = {"Instant Sell","Sell Order"};
        JComboBox<String> jComboBox = new JComboBox<>(options);
        jComboBox.setBounds(0, 20, 185, 20);

        JButton button = new JButton("Start");
        button.setBounds(50, 40, 100, 20);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOption = jComboBox.getItemAt(jComboBox.getSelectedIndex());
                if(selectedOption.equals("Instant Sell"))
                    sellByQuickSell = true;
                if(selectedOption.equals("Sell Order"))
                    sellByOrder = true;
                if(sellByQuickSell || sellByOrder) {
                    canStart = true;
                    frame.setVisible(false);
                }
            }
        });
        frame.setLayout(null);

        frame.add(label);
        frame.add(jComboBox);
        frame.add(button);
        frame.add(label1);
        frame.setVisible(true);
    }

    @Override
    public void onChatMessage(Component component, int i, int i1, boolean b) {
        String chat_message = component.getString();
        if(chat_message.contains("[Bazaar]")){
            shouldCollect = true;
        }
        if(chat_message.equals("Somehow, there was nothing to claim!")){
            failedClaim = true;
        }
    }

    @Override
    public int poll(){

        try {
            String currLocation = SkyBlock.getScoreboardLocation();
            if (currLocation.equals("Your Island")) {
                Game.sendChatMessage("/warp hub");
                return 5000;
            }
        } catch (Exception e) {
            if (SkyBlock.isInLimbo()) {
                Game.sendChatMessage("/lobby");
                return 2000;
            }
        }
        if(!SkyBlock.isPlayingSkyBlock()) {
            Game.sendChatMessage("/skyblock");
            return 2000;
        }

        if(!canStart)
            return 2000;

        if (doFailedClaim) {
            if (SkyBlockBazaar.isOpen()) {
                if (SkyBlockBazaar.getCurrentCategory() != SkyBlockBazaar.Category.FARMING) {
                    SkyBlockBazaar.selectSubcategory("Carrot");
                    doFailedClaim = false;
                    failedClaim = false;
                    return 500;
                }
                SkyBlockBazaar.Button.MANAGE_ORDERS.use();
                return 500;
            } else {
                SkyBlockBazaar.open();
                return 500;
            }
        }

        if(Inventory.isPlayerInventoryFull()){
            if(SkyBlockBazaar.isOpen()) {
                if (sellByOrder) {
                    if (SkyBlockBazaar.SellOrderConfirmScreen.isSellingConfirmScreen()) {
                        SkyBlockBazaar.SellOrderConfirmScreen.Button.SELL_OFFER.use();
                        Input.INVENTORY.click();
                        return 2000;
                    }
                    if (SkyBlockBazaar.SellOrderPriceScreen.isSellingPriceScreen()) {
                        SkyBlockBazaar.SellOrderPriceScreen.Button.BEST_OFFER_1.use();
                        return 500;
                    }
                    if (SkyBlockBazaar.isTradeScreenOpen()) {
                        SkyBlockBazaar.TradeOfferScreen.Button.CREATE_SELL_OFFER.use();
                        return 500;
                    }
                    if (SkyBlockBazaar.isSubCategoryScreen()) {
                        SkyBlockBazaar.selectSubcategory("Carrot");
                        return 500;
                    }
                    if (SkyBlockBazaar.isCategoryScreen()) {
                        SkyBlockBazaar.selectCategory(SkyBlockBazaar.Category.FARMING);
                        SkyBlockBazaar.selectSubcategory("Carrot");
                        return 500;
                    }
                    return 500;

                }
                if (sellByQuickSell) {
                    if (SkyBlockBazaar.SellEntireInventory.isConfirmScreen()) {
                        SkyBlockBazaar.SellEntireInventory.Confirm.CONFIRM.use();
                        return 2000;
                    }
                    SkyBlockBazaar.Button.SELL_INVENTORY_NOW.use();
                    return 500;
                }
            } else {
                SkyBlockBazaar.open();
                return 2000;
            }
        } else {
            if(shouldCollect && sellByOrder && PlayerHelper.playerFeet().distance(bazaar) < 10) {
                if (SkyBlockBazaar.isOpen()) {
                    if (SkyBlockBazaar.getCurrentCategory() != SkyBlockBazaar.Category.FARMING) {
                        SkyBlockBazaar.selectSubcategory("Carrot");
                        shouldCollect = false;
                        return 500;
                    }
                    SkyBlockBazaar.Button.MANAGE_ORDERS.use();
                    return 500;
                } else {
                    SkyBlockBazaar.open();
                    return 500;
                }
            }
        }

        if(currentFarm == farms.length){
            currentFarm = 0;
            Game.sendChatMessage("/warp home");
            return 2000;
        }

        if(PlayerHelper.playerFeet().distance(farms[currentFarm]) < 2 && counter <= 10) {
            BlockState carrot = Blocks.getClosest(z -> z.getName().equals("Carrots"), 20);
            if (carrot != null) {
                BlockPos carrotBlock = carrot.getBlockPosition();
                Input.CLICK_LEFT.setHoldingDown(true);
                Camera.smoothTurn(carrotBlock);
                counter++;
                if (counter == 10) {
                    Input.CLICK_LEFT.setHoldingDown(false);
                    currentFarm++;
                    return 100;
                }
                return 50;
            }
        }
        Movement.walkTo(farms[currentFarm]);
        counter = 0;
        return 500;
    }
}
