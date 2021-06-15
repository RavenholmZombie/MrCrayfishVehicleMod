package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.RayTraceFunction;
import com.mrcrayfish.vehicle.common.ItemLookup;
import com.mrcrayfish.vehicle.entity.EngineTier;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.entity.WheelType;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractPoweredRenderer<T extends PoweredVehicleEntity & EntityRayTracer.IEntityRayTraceable> extends AbstractVehicleRenderer<T>
{
    protected final PropertyFunction<T, Boolean> renderEngineProperty = new PropertyFunction<>(PoweredVehicleEntity::shouldRenderEngine, true);
    protected final PropertyFunction<T, Boolean> hasEngineProperty = new PropertyFunction<>(PoweredVehicleEntity::hasEngine, true);
    protected final PropertyFunction<T, EngineTier> engineTierProperty = new PropertyFunction<>(PoweredVehicleEntity::getEngineTier, EngineTier.WOOD);
    protected final PropertyFunction<T, Boolean> renderFuelPortProperty = new PropertyFunction<>(PoweredVehicleEntity::shouldRenderFuelPort, true);
    protected final PropertyFunction<T, Boolean> requiresFuelProperty = new PropertyFunction<>(PoweredVehicleEntity::requiresFuel, true);
    protected final PropertyFunction<T, Integer> wheelColorProperty = new PropertyFunction<>(PoweredVehicleEntity::getWheelColor, -1);
    protected final PropertyFunction<T, Boolean> hasWheelsProperty = new PropertyFunction<>(PoweredVehicleEntity::hasWheels, true);
    protected final PropertyFunction<T, WheelType> wheelTypeProperty = new PropertyFunction<>(PoweredVehicleEntity::getWheelType, WheelType.STANDARD);

    public AbstractPoweredRenderer(VehicleProperties defaultProperties)
    {
        super(defaultProperties);
    }

    public void setRenderEngine(boolean renderEngine)
    {
        this.renderEngineProperty.setDefaultValue(renderEngine);
    }

    public void setHasEngine(boolean hasEngine)
    {
        this.renderEngineProperty.setDefaultValue(hasEngine);
    }

    public void setEngineTier(EngineTier engineTier)
    {
        this.engineTierProperty.setDefaultValue(engineTier);
    }

    public void setRenderFuelPort(boolean renderFuelPort)
    {
        this.renderFuelPortProperty.setDefaultValue(renderFuelPort);
    }

    public void setRequiresFuel(boolean requiresFuel)
    {
        this.requiresFuelProperty.setDefaultValue(requiresFuel);
    }

    public void setHasWheels(boolean hasWheels)
    {
        this.hasWheelsProperty.setDefaultValue(hasWheels);
    }

    public void setWheelType(WheelType type)
    {
        this.wheelTypeProperty.setDefaultValue(type);
    }

    public void setWheelColor(int wheelColor)
    {
        this.wheelColorProperty.setDefaultValue(wheelColor);
    }

    protected void renderEngine(@Nullable T vehicle, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light)
    {
        if(this.renderEngineProperty.get(vehicle) && this.hasEngineProperty.get(vehicle))
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            IBakedModel engineModel = RenderUtil.getModel(ItemLookup.getEngine(properties.getEngineType(), this.engineTierProperty.get(vehicle)));
            this.renderEngine(vehicle, properties.getEnginePosition(), engineModel, matrixStack, renderTypeBuffer, light);
        }
    }

    protected void renderFuelPort(@Nullable T vehicle, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light)
    {
        if(vehicle != null && vehicle.shouldRenderFuelPort() && vehicle.requiresFuel())
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            PoweredVehicleEntity.FuelPortType fuelPortType = vehicle.getFuelPortType();
            EntityRayTracer.RayTraceResultRotated result = EntityRayTracer.instance().getContinuousInteraction();
            if(result != null && result.getType() == RayTraceResult.Type.ENTITY && result.getEntity() == vehicle && result.equalsContinuousInteraction(RayTraceFunction.FUNCTION_FUELING))
            {
                this.renderPart(properties.getFuelPortPosition(), fuelPortType.getOpenModel().getModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
                if(this.shouldRenderFuelLid())
                {
                    //this.renderPart(properties.getFuelPortLidPosition(), entity.fuelPortLid);
                }
                vehicle.playFuelPortOpenSound();
            }
            else
            {
                this.renderPart(properties.getFuelPortPosition(), fuelPortType.getClosedModel().getModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
                vehicle.playFuelPortCloseSound();
            }
        }
    }

    protected void renderKeyPort(@Nullable T vehicle, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light)
    {
        if(vehicle != null && vehicle.isKeyNeeded())
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            this.renderPart(properties.getKeyPortPosition(), this.getKeyHoleModel().getModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
            if(!vehicle.getKeyStack().isEmpty())
            {
                this.renderKey(properties.getKeyPosition(), vehicle.getKeyStack(), RenderUtil.getModel(vehicle.getKeyStack()), matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}