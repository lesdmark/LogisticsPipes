package logisticspipes.renderer;

import java.lang.reflect.Field;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.SlotFinderNumberPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.SimpleGraphics;

public class LogisticsGuiOverrenderer {

	@Getter
	private static LogisticsGuiOverrenderer instance = new LogisticsGuiOverrenderer();

	private int oldX;
	private int oldY;
	private boolean hasBeenSaved;
	private boolean clicked;
	private Field fX;
	private Field fY;
	@Setter
	private int targetPosX;
	@Setter
	private int targetPosY;
	@Setter
	private int targetPosZ;
	@Setter
	private int pipePosX;
	@Setter
	private int pipePosY;
	@Setter
	private int pipePosZ;
	@Setter
	private ModulePositionType positionType;
	@Setter
	private int positionInt;
	@Setter
	private int slot;
	@Setter
	private boolean isOverlaySlotActive;

	private LogisticsGuiOverrenderer() {
		try {
			fX = Mouse.class.getDeclaredField("x");
			fY = Mouse.class.getDeclaredField("y");
			fX.setAccessible(true);
			fY.setAccessible(true);
		} catch (Exception e) {
			if (LogisticsPipes.isDEBUG()) {
				e.printStackTrace();
			}
		}
	}

	public boolean isCompatibleGui() {
		if (FMLClientHandler.instance() == null) {
			return false;
		}
		if (FMLClientHandler.instance().getClient() == null) {
			return false;
		}
		if (!(FMLClientHandler.instance().getClient().currentScreen instanceof GuiContainer)) {
			return false;
		}
		return true;
	}

	public void preRender() {
		if (isOverlaySlotActive) {
			// Save Mouse Pos
			oldX = Mouse.getX();
			oldY = Mouse.getY();
			// Set Pos 0,0
			try {
				fX.set(null, 0);
				fY.set(null, 0);
				hasBeenSaved = true;
			} catch (Exception e) {
				if (LogisticsPipes.isDEBUG()) {
					e.printStackTrace();
				}
			}
			while (Mouse.next()) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
					clicked = true;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void renderOverGui() {
		if (hasBeenSaved) {
			hasBeenSaved = false;
			// Resore Mouse Pos
			try {
				fX.set(null, oldX);
				fY.set(null, oldY);
			} catch (Exception e) {
				if (LogisticsPipes.isDEBUG()) {
					e.printStackTrace();
				}
			}
		}
		if (isOverlaySlotActive) {
			GuiContainer gui = (GuiContainer) FMLClientHandler.instance().getClient().currentScreen;
			int guiTop = gui.getGuiTop();
			int guiLeft = gui.getGuiLeft();
			int x = oldX * gui.width / FMLClientHandler.instance().getClient().displayWidth;
			int y = gui.height - oldY * gui.height / FMLClientHandler.instance().getClient().displayHeight - 1;
			for (Slot slot : gui.inventorySlots.inventorySlots) {
				if (isMouseOverSlot(gui, slot, x, y)) {
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glTranslated(guiLeft, guiTop, 0);
					int k1 = slot.xPos;
					int i1 = slot.yPos;
					SimpleGraphics.drawGradientRect(k1, i1, k1 + 16, i1 + 16, 0xa0ff0000, 0xa0ff0000, 0.0);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					if (clicked) {
						MainProxy.sendPacketToServer(PacketHandler.getPacket(SlotFinderNumberPacket.class).setInventorySlot(slot.slotNumber).setSlot(this.slot).setPipePosX(pipePosX).setPipePosY(pipePosY).setPipePosZ(pipePosZ).setType(positionType).setPositionInt(positionInt).setPosX(targetPosX).setPosY(targetPosY)
								.setPosZ(targetPosZ));
						clicked = false;
						FMLClientHandler.instance().getClient().player.closeScreen();
						isOverlaySlotActive = false;
					}
					break;
				}
			}
			clicked = false;
		}
	}

	private boolean isMouseOverSlot(GuiContainer gui, Slot slot, int mouseX, int mouseY) {
		return isPointInRegion(gui, slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
	}

	private boolean isPointInRegion(GuiContainer gui, int x, int y, int width, int height, int pointX, int pointY) {
		int x0 = gui.getGuiLeft();
		int y0 = gui.getGuiTop();
		pointX -= x0;
		pointY -= y0;
		return pointX >= x - 1 && pointX < x + width + 1 && pointY >= y - 1 && pointY < y + height + 1;
	}
}
