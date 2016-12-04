package au.com.addstar.monolith.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;

import com.google.common.collect.Lists;

import au.com.addstar.monolith.util.Attributes;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagList;

public class MonoItemAttributes implements ItemAttributes
{
	private static final String KeyUUID = "UUID";
	private static final String KeyAmount = "Amount";
	private static final String KeyOperation = "Operation";
	private static final String KeyName = "Name";
	private static final String KeySlot = "Slot";
	private static final String KeyAttributeName = "AttributeName";
	
	private final NBTTagList list;
	
	public MonoItemAttributes(NBTTagList list)
	{
		this.list = list;
	}
	
	@Override
	public void addModifier(Attribute attribute, ItemAttributeModifier modifier)
	{
		// Check that the UUID is unique
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (modifier.getUniqueId().equals(tag.a(KeyUUID)))
				throw new IllegalArgumentException("UUID must be unique across all modifiers");
		}
		
		// Add the modifier
		NBTTagCompound modifierTag = saveModifier(attribute, modifier);
		list.add(modifierTag);
	}

	@Override
	public void removeModifier(ItemAttributeModifier modifier)
	{
		// Find the modifier
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (modifier.getUniqueId().equals(tag.a(KeyUUID)))
			{
				// Remove it
				list.remove(i);
				break;
			}
		}
	}

	@Override
	public void clearModifiers(Attribute attribute)
	{
		String id = Attributes.getId(attribute);
		
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (id.equals(tag.getString(KeyAttributeName)))
			{
				// Remove it
				list.remove(i);
				--i;
			}
		}
	}

	@Override
	public void clearModifiers()
	{
		for (int i = 0; i < list.size(); ++i)
			list.remove(0);
	}

	@Override
	public ItemAttributeModifier getModifier(UUID id)
	{
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (id.equals(tag.a(KeyUUID)))
				return readModifier(tag);
		}
		
		return null;
	}

	@Override
	public Collection<ItemAttributeModifier> getModifiers(String name)
	{
		List<ItemAttributeModifier> modifiers = Lists.newArrayList();
		
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (name.equals(tag.getString(KeyName)))
				modifiers.add(readModifier(tag));
		}
		
		return Collections.unmodifiableList(modifiers);
	}
	
	@Override
	public Collection<ItemAttributeModifier> getModifiers(Attribute attribute)
	{
		String id = Attributes.getId(attribute);
		List<ItemAttributeModifier> modifiers = Lists.newArrayList();
		
		for (int i = 0; i < list.size(); ++i)
		{
			NBTTagCompound tag = list.get(i);
			if (id.equals(tag.getString(KeyAttributeName)))
				modifiers.add(readModifier(tag));
		}
		
		return Collections.unmodifiableList(modifiers);
	}

	private static ItemAttributeModifier readModifier(NBTTagCompound tag)
	{
		String name = tag.getString(KeyName);
		UUID id = tag.a(KeyUUID);
		double amount = tag.getDouble(KeyAmount);
		int op = tag.getInt(KeyOperation);
		String rawSlot = tag.getString(KeySlot);
		
		EquipmentSlot slot = null;
		if (rawSlot != null)
		{
			switch (rawSlot)
			{
			case "mainhand":
				slot = EquipmentSlot.HAND;
				break;
			case "offhand":
				slot = EquipmentSlot.OFF_HAND;
				break;
			case "head":
				slot = EquipmentSlot.HEAD;
				break;
			case "chest":
				slot = EquipmentSlot.CHEST;
				break;
			case "legs":
				slot = EquipmentSlot.LEGS;
				break;
			case "feet":
				slot = EquipmentSlot.FEET;
				break;
			}
		}
		
		return new ItemAttributeModifier(id, name, amount, Operation.values()[op], slot);
	}
	
	private static NBTTagCompound saveModifier(Attribute attribute, ItemAttributeModifier modifier)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString(KeyName, modifier.getName());
		tag.a(KeyUUID, modifier.getUniqueId());
		tag.setDouble(KeyAmount, modifier.getAmount());
		tag.setInt(KeyOperation, modifier.getOperation().ordinal());
		tag.setString(KeyAttributeName, Attributes.getId(attribute));

        EquipmentSlot slot = modifier.getSlot();
        if (slot != null)
		{
			switch (slot)
			{
			case CHEST:
				tag.setString(KeySlot, "chest");
				break;
			case FEET:
				tag.setString(KeySlot, "feet");
				break;
			case HAND:
				tag.setString(KeySlot, "mainhand");
				break;
			case HEAD:
				tag.setString(KeySlot, "head");
				break;
			case LEGS:
				tag.setString(KeySlot, "legs");
				break;
			case OFF_HAND:
				tag.setString(KeySlot, "offhand");
				break;
			default:
				break;
			}
		}
		
		return tag;
	}

}