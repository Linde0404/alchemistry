package al132.alchemistry.recipes

import al132.alchemistry.items.ModItems
import al132.alib.tiles.ALTileStackHandler
import al132.alib.utils.Utils.areItemsEqualIgnoreMeta
import al132.alib.utils.extensions.areItemStacksEqual
import al132.alib.utils.extensions.copy
import al132.alib.utils.extensions.get
import al132.alib.utils.extensions.toStackList
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.oredict.OreDictionary
import java.util.*

/**
 * Created by al132 on 1/22/2017.
 */

data class CombinerRecipe(private val _output: ItemStack, private val objsIn: List<Any?>, var gamestage: String = "") {

    val inputs: List<ItemStack>
        get() = inputsInternal.toList().copy()

    val output: ItemStack
        get() = _output.copy()

    private val inputsInternal = ArrayList<ItemStack>()

    init {
        val tempInputs = objsIn
        (0 until INPUT_COUNT).forEach { index ->
            val tempInput = tempInputs.getOrNull(index)

            when (tempInput) {
                is ItemStack -> inputsInternal.add(tempInput)
                is Item      -> inputsInternal.add(ItemStack(tempInput))
                is Block     -> inputsInternal.add(ItemStack(tempInput))
                is String    -> {
                } //TODO oredict input
                else         -> inputsInternal.add(ItemStack.EMPTY)
            }
        }
    }

    fun matchesHandlerStacks(handler: ALTileStackHandler): Boolean {
        var matchingStacks = 0

        for ((index: Int, recipeStack: ItemStack) in this.inputs.withIndex()) {
            val handlerStack = handler[index]
            if ((handlerStack.item == ModItems.slotFiller || handlerStack.isEmpty) && recipeStack.isEmpty) matchingStacks++
            else if (handlerStack.isEmpty || recipeStack.isEmpty) continue
            else if (areItemsEqualIgnoreMeta(handlerStack, recipeStack)
                    && handlerStack.count >= recipeStack.count
                    && (handlerStack.itemDamage == recipeStack.itemDamage || recipeStack.itemDamage == OreDictionary.WILDCARD_VALUE)) {
                matchingStacks++
            }
        }
        return (matchingStacks == CombinerRecipe.INPUT_COUNT)
    }

    companion object {

        private const val INPUT_COUNT = 9

        fun matchInputs(handler: IItemHandler): CombinerRecipe? {
            assert(handler.slots == INPUT_COUNT)
            return matchInputs(handler.toStackList())
        }

        fun matchInputs(inputStacks: List<ItemStack>): CombinerRecipe? {
            for (recipe in ModRecipes.combinerRecipes) {
                var matchingStacks = 0
                for ((index: Int, recipeStack: ItemStack) in recipe.inputs.withIndex()) {
                    val inputStack: ItemStack = inputStacks[index]

                    if ((inputStack.item == ModItems.slotFiller || inputStack.isEmpty) && recipeStack.isEmpty) matchingStacks++
                    else if (inputStack.isEmpty || recipeStack.isEmpty) continue
                    else if (areItemsEqualIgnoreMeta(inputStack, recipeStack)
                            && inputStack.count >= recipeStack.count
                            && (inputStack.itemDamage == recipeStack.itemDamage || recipeStack.itemDamage == OreDictionary.WILDCARD_VALUE)) {
                        matchingStacks++
                    }
                }
                if (matchingStacks == INPUT_COUNT) return recipe.copy()
            }
            return null
        }

        fun matchOutput(stack: ItemStack): CombinerRecipe? {
            return ModRecipes.combinerRecipes.firstOrNull { it.output.areItemStacksEqual(stack) }?.copy()
        }
    }
}