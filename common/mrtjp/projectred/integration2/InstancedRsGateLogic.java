package mrtjp.projectred.integration2;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import mrtjp.projectred.ProjectRedIntegration;
import mrtjp.projectred.core.Configurator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class InstancedRsGateLogic extends RedstoneGateLogic<InstancedRsGatePart>
{
    public static InstancedRsGateLogic create(InstancedRsGatePart gate, int subID) {
        switch(subID) {
            case 12:
                return new RSLatch(gate);
            case 13:
                return new ToggleLatch(gate);
            case 17:
                return new Timer(gate);
        }
        throw new IllegalArgumentException("Invalid subID: "+subID);
    }
    
    public InstancedRsGateLogic(InstancedRsGatePart gate) {
        this.gate = gate;
    }
    
    public InstancedRsGatePart gate;
    
    @Override
    public int getOutput(InstancedRsGatePart gate, int r) {
        return (gate.state & 0x10<<r) != 0 ? 15 : 0;
    }
    
    public void save(NBTTagCompound tag) {
    }
    
    public void load(NBTTagCompound tag) {
    }

    public void readDesc(MCDataInput packet) {
    }
    
    public void writeDesc(MCDataOutput packet) {
    }
    
    public void read(MCDataInput packet, int switch_key) {
        
    }
    
    public static abstract class ExtraStateLogic extends InstancedRsGateLogic
    {
        public byte state2;
        
        public ExtraStateLogic(InstancedRsGatePart gate) {
            super(gate);
        }
        
        public int state2() {
            return state2&0xFF;
        }
        
        public void setState2(int i) {
            state2 = (byte)i;
        }
        
        @Override
        public void save(NBTTagCompound tag) {
            tag.setByte("state2", state2);
        }
        
        @Override
        public void load(NBTTagCompound tag) {
            state2 = tag.getByte("state2");
        }
        
        @Override
        public void readDesc(MCDataInput packet) {
            if(clientState2())
                state2 = packet.readByte();
        }
        
        @Override
        public void writeDesc(MCDataOutput packet) {
            if(clientState2())
                packet.writeByte(state2);
        }
        
        @Override
        public void read(MCDataInput packet, int switch_key) {
            if(switch_key == 11)
                state2 = packet.readByte();
        }
        
        public boolean clientState2() {
            return false;
        }
        
        public void sendState2Update(GatePart gate) {
            gate.getWriteStream(11).writeByte(state2);
        }
    }
    
    public static class RSLatch extends ExtraStateLogic
    {
        public RSLatch(InstancedRsGatePart gate) {
            super(gate);
        }
        

        @Override
        public boolean cycleShape(InstancedRsGatePart gate) {
            int newShape = (gate.shape()+1)%4;
            gate.setShape(newShape);
            setState2(GatePart.flipMaskZ(state2()));
            gate.setState(GatePart.flipMaskZ(gate.state()));
            gate.onOutputChange(0xF);
            gate.scheduleTick(2);
            return true;
        }
        
        @Override
        public int outputMask(int shape) {
            return shape >> 1 == 0 ? 0xF : 5;
        }
        
        @Override
        public int inputMask(int shape) {
            return 0xA;
        }
        
        @Override
        public void setup(InstancedRsGatePart gate) {
            setState2(2);
            gate.setState(0x30);
            gate.onOutputChange(0x30);
        }
        
        @Override
        public void onChange(InstancedRsGatePart gate) {
            int stateInput = state2();
            
            int oldInput = gate.state()&0xF;
            int newInput = getInput(gate, 0xA);
            int oldOutput = gate.state()>>4;
            
            if(newInput != oldInput) {
                if(stateInput != 0xA && newInput != 0 && newInput != state2()) {//state needs changing.
                    gate.setState(newInput);
                    setState2(newInput);
                    gate.onOutputChange(oldOutput);//always going low
                    gate.scheduleTick(2);
                }
                else {
                    gate.setState(oldOutput<<4 | newInput);
                    gate.onInputChange();
                }
            }
        }
        
        @Override
        public void scheduledTick(InstancedRsGatePart gate) {
            int oldOutput = gate.state()>>4;
            int newOutput = calcOutput(gate);
            if(oldOutput != newOutput) {
                gate.setState(gate.state() & 0xF | newOutput<<4);
                gate.onOutputChange(outputMask(gate.shape()));
            }
            onChange(gate);
        }
        
        public int calcOutput(SimpleGatePart gate) {
            int input = gate.state()&0xF;
            int stateInput = state2();
            
            if((gate.shape & 1) != 0) {//reverse
                input = GatePart.flipMaskZ(input);
                stateInput = GatePart.flipMaskZ(stateInput);
            }
            
            if(stateInput == 0xA) {//disabled
                if(input == 0xA) {
                    gate.scheduleTick(2);
                    return 0;
                }
                
                if(input == 0)
                    stateInput = gate.world().rand.nextBoolean() ? 2 : 8;
                else
                    stateInput = input;
                setState2(stateInput);
            }
            
            int output = GatePart.shiftMask(stateInput, 1);
            if((gate.shape & 2) == 0)
                output |= stateInput;

            if((gate.shape & 1) != 0)//reverse
                output = GatePart.flipMaskZ(output);
            
            return output;
        }
    }
    
    public static class ToggleLatch extends ExtraStateLogic
    {
        public ToggleLatch(InstancedRsGatePart gate) {
            super(gate);
        }

        @Override
        public int outputMask(int shape) {
            return 5;
        }
        
        @Override
        public int inputMask(int shape) {
            return 0xA;
        }
        
        @Override
        public boolean clientState2() {
            return true;
        }
        
        @Override
        public void setup(InstancedRsGatePart gate) {
            gate.setState(0x10);
            gate.onOutputChange(1);
        }
        
        @Override
        public void onChange(InstancedRsGatePart gate) {
            int oldInput = gate.state()&0xF;
            int newInput = getInput(gate, 0xA);
            int high = newInput & ~oldInput;
            
            if(high == 2 || high == 8)//one side went high (if both, double change so no change)
                toggle(gate);
            
            if(oldInput != newInput) {
                gate.setState(gate.state() & 0xF0 | newInput);
                gate.onInputChange();
            }
        }

        @Override
        public void scheduledTick(InstancedRsGatePart gate) {
            int oldOutput = gate.state()>>4;
            int newOutput = state2 == 0 ? 1 : 4;
            if(oldOutput != newOutput) {
                gate.setState(newOutput<<4 | gate.state() & 0xF);
                gate.onOutputChange(outputMask(5));
            }
            onChange(gate);
        }
        
        @Override
        public boolean activate(InstancedRsGatePart gate, EntityPlayer player, ItemStack held) {
            if(held == null || held.getItem() != ProjectRedIntegration.itemScrewdriver) {
                if(!gate.world().isRemote)
                    toggle(gate);
                return true;
            }
            return false;
        }

        private void toggle(InstancedRsGatePart gate) {
            setState2(state2^1);
            sendState2Update(gate);
            gate.scheduleTick(2);
            if (Configurator.logicGateSounds.getBoolean(true))
                gate.world().playSoundEffect(gate.x(), gate.y(), gate.z(), "random.click", 0.3F, 0.5F);
        }
    }
    
    public static abstract class TimerGateLogic extends InstancedRsGateLogic implements ITimerGuiLogic
    {
        public int pointer_max = 38;
        public long pointer_start = -1;

        public TimerGateLogic(InstancedRsGatePart gate) {
            super(gate);
        }
        
        @Override
        public void save(NBTTagCompound tag) {
            tag.setInteger("pmax", pointer_max);
            tag.setLong("pstart", pointer_start);
        }
        
        @Override
        public void load(NBTTagCompound tag) {
            pointer_max = tag.getInteger("pmax");
            pointer_start = tag.getLong("pstart");
        }
        
        @Override
        public void writeDesc(MCDataOutput packet) {
            packet.writeInt(pointer_max);
            packet.writeLong(pointer_start);
        }
        
        @Override
        public void readDesc(MCDataInput packet) {
            pointer_max = packet.readInt();
            pointer_start = packet.readLong();
        }
        
        @Override
        public void read(MCDataInput packet, int switch_key) {
            if(switch_key == 11)
                pointer_max = packet.readInt();
            else if(switch_key == 12) {
                pointer_start = packet.readInt();
                if(pointer_start >= 0)
                    pointer_start = gate.world().getWorldTime()-pointer_start;
            }
        }
        
        public int pointerValue() {
            if(pointer_start < 0)
                return 0;
            
            return (int)(gate.world().getWorldTime()-pointer_start);
        }
        
        public void sendPointerMaxUpdate() {
            gate.getWriteStream(11).writeInt(pointer_max);
        }
        
        public void sendPointerUpdate() {
            gate.getWriteStream(12).writeInt(pointer_start < 0 ? -1 : pointerValue());
        }
        
        @Override
        public int getTimerMax() {
            return pointer_max+2;
        }
        
        @Override
        public void setTimerMax(GatePart gate, int t) {
            if(t < 4)
                t = 4;
            if(t != pointer_max) {
                pointer_max = t-2;
                sendPointerMaxUpdate();
            }
        }
        
        @Override
        public void onTick(InstancedRsGatePart gate) {
            if(pointer_start >= 0 && gate.world().getWorldTime() >= pointer_start+pointer_max)
                pointerTick();
        }
        
        public abstract void pointerTick();
        
        public void resetPointer() {
            if(pointer_start >= 0) {
                pointer_start = -1;
                if(!gate.world().isRemote)
                    sendPointerUpdate();
            }
        }
        
        public void startPointer() {
            if(pointer_start < 0) {
                pointer_start = gate.world().getWorldTime();
                if(!gate.world().isRemote)
                    sendPointerUpdate();
            }
        }
        
        public float interpPointer(float f) {
            if(pointer_start < 0)
                return 0;
            
            return (pointerValue()+f)/pointer_max;
        }
    }
    
    public static class Timer extends TimerGateLogic
    {
        public Timer(InstancedRsGatePart gate) {
            super(gate);
        }
        
        @Override
        public int outputMask(int shape) {
            return 0xB;
        }
        
        @Override
        public int inputMask(int shape) {
            return 0xE;
        }
        
        @Override
        public void setup(InstancedRsGatePart gate) {
            startPointer();
        }
        
        @Override
        public void scheduledTick(InstancedRsGatePart gate) {
            gate.setState(gate.state()&0xF);
            gate.onOutputChange(0xB);
            onChange(gate);
        }
        
        @Override
        public void onChange(InstancedRsGatePart gate) {
            int oldInput = gate.state()&0xF;
            int newInput = getInput(gate, 0xE);
            if(newInput != oldInput) {
                gate.setState(gate.state()&0xF0 | newInput);
                gate.onInputChange();
            }
            
            if(gate.schedTime < 0) {
                if(newInput > 0)
                    resetPointer();
                else
                    startPointer();
            }
        }
        
        @Override
        public void pointerTick() {
            resetPointer();
            if(!gate.world().isRemote) {
                gate.setState(0xB0 | gate.state()&0xF);
                gate.onOutputChange(0xB);
                gate.scheduleTick(2);
            }
        }
        
        @Override
        public boolean activate(InstancedRsGatePart gate, EntityPlayer player, ItemStack held) {
            if(held == null || held.getItem() != ProjectRedIntegration.itemScrewdriver) {
                if(!gate.world().isRemote)
                    IntegrationSPH.openTimerGui(player, gate);
                
                return true;
            }
            return false;
        }
    }
}
