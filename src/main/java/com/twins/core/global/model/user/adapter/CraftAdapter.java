package com.twins.core.global.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.utils.ItemSerializer;
import com.twins.core.global.model.craft.Craft;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;

public class CraftAdapter implements DatabaseAdapter<Craft> {

    @Override
    public Craft adapt(DatabaseQuery databaseQuery) throws SQLException {

        String itemBase64 = (String) databaseQuery.get("craftItem");

        if (itemBase64 == null) {
            return null;
        }

        try {

            ItemStack item = ItemSerializer.itemStackFromBase64(itemBase64);
            int amount = (int) databaseQuery.get("craftAmount");
            int delay = (int) databaseQuery.get("craftDelay");

            return new Craft(item, amount, delay);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
