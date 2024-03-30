package satisfy.farm_and_charm.client.recipebook;

import de.cristelknight.doapi.client.recipebook.screen.widgets.PrivateRecipeBookWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import satisfy.farm_and_charm.recipe.CookingPotRecipe;
import satisfy.farm_and_charm.registry.RecipeTypesRegistry;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CookingSaucePanRecipeBook extends PrivateRecipeBookWidget {
    private static final Component TOGGLE_COOKABLE_TEXT;

    public CookingSaucePanRecipeBook() {
    }

    @Override
    public void showGhostRecipe(Recipe<?> recipe, List<Slot> slots,  RegistryAccess registryAccess) {
        this.ghostSlots.addSlot(recipe.getResultItem(registryAccess), slots.get(0).x, slots.get(0).y);
        if (recipe instanceof CookingPotRecipe cookingPotRecipe) {
            this.ghostSlots.addSlot(cookingPotRecipe.getContainer(), slots.get(7).x, slots.get(7).y);
        }
        int j = 1;
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack[] inputStacks = ingredient.getItems();
            if (inputStacks.length == 0) continue;
            ItemStack inputStack = inputStacks[RandomSource.create().nextInt(0, inputStacks.length)];
            this.ghostSlots.addSlot(inputStack, slots.get(j).x, slots.get(j++).y);
        }
    }

    @Override
    public void insertRecipe(Recipe<?> r) {
        if(r instanceof CookingPotRecipe recipe){
            int usedInputSlots = 1;
            for (Ingredient ingredient : recipe.getIngredients()) {
                int slotIndex = 0;
                for (Slot slot : screenHandler.slots) {
                    ItemStack itemStack = slot.getItem();

                    if (ingredient.test(itemStack) && usedInputSlots < 7) {
                        Minecraft.getInstance().gameMode.handleInventoryMouseClick(screenHandler.containerId, slotIndex, 0, ClickType.PICKUP, Minecraft.getInstance().player);
                        Minecraft.getInstance().gameMode.handleInventoryMouseClick(screenHandler.containerId, usedInputSlots, 0, ClickType.PICKUP, Minecraft.getInstance().player);
                        ++usedInputSlots;
                        break;
                    }
                    ++slotIndex;
                }
            }

            int slotIndex = 0;
            for (Slot slot : screenHandler.slots) {
                ItemStack itemStack = slot.getItem();

                if (ItemStack.isSameItem(recipe.getContainer(), itemStack)) {
                    Minecraft.getInstance().gameMode.handleInventoryMouseClick(screenHandler.containerId, slotIndex, 0, ClickType.PICKUP, Minecraft.getInstance().player);
                    Minecraft.getInstance().gameMode.handleInventoryMouseClick(screenHandler.containerId, 7, 0, ClickType.PICKUP, Minecraft.getInstance().player);
                    break;
                }
                ++slotIndex;
            }
        }
    }

    @Override
    protected void setCraftableButtonTexture() {
        this.toggleCraftableButton.initTextureValues(152, 41, 28, 18, TEXTURE);
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
        super.slotClicked(slot);
        if (slot != null && slot.index < this.screenHandler.getCraftingSlotCount()) {
            this.ghostSlots.reset();
        }
    }

    @Override
    protected RecipeType<? extends Recipe<Container>> getRecipeType() {
        return RecipeTypesRegistry.COOKING_SAUCEPAN_RECIPE_TYPE.get();
    }

    @Override
    protected Component getToggleCraftableButtonText() {
        return TOGGLE_COOKABLE_TEXT;
    }

    static {
        TOGGLE_COOKABLE_TEXT = Component.translatable("gui.farm_and_charm.recipebook.toggleRecipes.cookable");
    }


    @Override
    public void setFocused(boolean bl) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
