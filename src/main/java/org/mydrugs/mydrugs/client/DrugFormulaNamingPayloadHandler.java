package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.menu.client.DrugFormulaNamingScreen;
import org.mydrugs.mydrugs.network.OpenDrugFormulaNamingPayload;

public final class DrugFormulaNamingPayloadHandler {
    private DrugFormulaNamingPayloadHandler() {
    }

    public static void handle(OpenDrugFormulaNamingPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new DrugFormulaNamingScreen(payload.formula()));
    }
}
