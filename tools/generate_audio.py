"""Original mechanical / low-register sound design, numpy + stdlib only.
Run from any directory. Reproducible PCM16 mono, 32000 Hz.
"""
from pathlib import Path
import wave,json
import numpy as np
SR=32000
ROOT=Path(__file__).resolve().parents[1]
DEST=ROOT/'src/assets/audio'
rng=np.random.default_rng(20261102)
report={}
def timeline(d):return np.arange(round(d*SR))/SR
def band(n,lo,hi):
    f=np.fft.rfftfreq(n,1/SR)
    weight=np.minimum(np.clip((f-lo)/max(20,lo*.3),0,1),np.clip((hi-f)/max(20,hi*.3),0,1))
    z=np.fft.rfft(rng.normal(size=n))*weight
    x=np.fft.irfft(z,n=n)
    return x/max(1e-9,np.std(x))
def sine(f,t):return np.sin(2*np.pi*f*t)
def fade(x,attack=.005,release=.04):
    x=x.copy();a=min(int(SR*attack),len(x)//2);b=min(int(SR*release),len(x)//2)
    if a:x[:a]*=np.sin(np.linspace(0,np.pi/2,a))**2
    if b:x[-b:]*=np.cos(np.linspace(0,np.pi/2,b))**2
    return x
def place(buf,sample,start,amp=1,wrap=False):
    offset=round(start*SR)
    if wrap:np.add.at(buf,(offset+np.arange(len(sample)))%len(buf),sample*amp)
    else:
        n=min(len(sample),len(buf)-offset)
        if n>0:buf[offset:offset+n]+=sample[:n]*amp

def tick(high=False):
    t=timeline(.18);f=1040 if high else 740
    # Escapement contact, wooden cabinet body, secondary pallet contact.
    x=.6*band(len(t),650,4300)*np.exp(-t*240)
    x+=.42*sine(f,t)*np.exp(-t*90)+.32*sine(260 if high else 205,t)*np.exp(-t*42)
    x+=.12*sine(f*2.19,t)*np.exp(-t*140)
    u=t-.012;x+=np.where(u>=0,.22*np.sin(2*np.pi*1650*u)*np.exp(-np.maximum(u,0)*160),0)
    return fade(x,.0008,.018)
def impact(d=.35,base=95):
    t=timeline(d)
    x=.8*sine(base,t)*np.exp(-t*16)+.24*sine(base*2.31,t)*np.exp(-t*24)
    x+=.3*band(len(t),130,1900)*np.exp(-t*55)
    return fade(x,.002,.06)
def write(kind,name,x,rms,peak=.65,loop=False):
    x=np.asarray(x,dtype=float);x-=np.mean(x)
    gain=min(rms/max(1e-12,np.sqrt(np.mean(x*x))),peak/max(1e-12,np.max(np.abs(x))))
    x*=gain
    if not loop:x=fade(x,.001,.02)
    pcm=np.rint(x*32767).astype('<i2')
    dest=DEST/kind/(name+'.wav');dest.parent.mkdir(parents=True,exist_ok=True)
    with wave.open(str(dest),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(SR);w.writeframes(pcm.tobytes())
    report[kind+'/'+name]={'seconds':len(x)/SR,'peak':float(np.max(np.abs(x))),'rms':float(np.sqrt(np.mean(x*x))),'boundary_jump':float(abs(x[-1]-x[0]))}
    assert np.isfinite(x).all() and np.max(np.abs(x))<.9

# Slow, restrained drones. All oscillations and modulations complete full loop cycles.
L=32;t=timeline(L)
for name,root,shade in [('menu',55,0),('present',58.25,1),('past',65.5,2),('future',49,3)]:
    x=np.zeros(len(t))
    for j,(ratio,amp) in enumerate([(1,.60),(1.5,.20),(2,.11),(3,.035)]):
        hz=round(root*ratio*L)/L
        env=.82+.18*np.cos(2*np.pi*t/L*(j%2+1)+shade+j)
        x+=amp*env*sine(hz,t)
    x+=.10*band(len(t),45,310)*(.75+.25*np.cos(2*np.pi*t/L))
    # Air / machinery texture instead of bright repeated melodies.
    x+=.025*band(len(t),350,1500)*(.5+.5*np.sin(2*np.pi*t/L+shade))
    write('bgm',name,x,.055,.27,True)

# Consistent two-beat pendulum: tick / tock 0.5 s apart, no musical beeps.
clock=np.zeros(len(t))
for i in range(64):place(clock,tick(i%2==1),.25+i*.5,wrap=True)
write('ambience','clockwork',clock,.042,.68,True)
rain=band(len(t),180,7200)*(.85+.15*np.cos(2*np.pi*t/L))
rain+=.22*band(len(t),40,500)
write('ambience','rain',rain,.032,.28,True)
reactor=.7*sine(48,t)+.22*sine(96,t)+.2*band(len(t),35,320)
reactor*=.85+.15*np.cos(2*np.pi*t/8)
write('ambience','reactor',reactor,.047,.26,True)
terminal=.4*sine(90,t)+.16*sine(180,t)+.14*band(len(t),100,600)
write('ambience','terminal',terminal,.022,.18,True)

# Dry mechanical UI: low contact sounds with short tails, no pitch sweeps.
write('sfx','ui_click',impact(.13,180),.075,.36)
write('sfx','file_select',impact(.11,235),.055,.28)
write('sfx','file_blocked',impact(.22,74),.08,.38)
x=np.zeros(len(timeline(.36)));place(x,impact(.22,115),0);place(x,tick(),.12,.2)
write('sfx','ui_open',x,.07,.38)
u=timeline(.19);x=.12*band(len(u),160,1600)*np.sin(np.pi*u/.19)**2
place(x,impact(.09,145),.09,.5)
write('sfx','file_move',x,.055,.3)
for name,d,base in [('item_pickup',.65,135),('core_pickup',1.05,70),('puzzle_success',1.4,110),('ending',2.8,65.5)]:
    u=timeline(d);x=np.zeros(len(u));place(x,impact(.25,base),.01,.65)
    env=(1-np.exp(-u*14))*np.exp(-u*3/d)
    x+=env*(.22*sine(base,u)+.10*sine(base*1.5,u)+.06*sine(base*2,u))
    write('sfx',name,fade(x,.01,min(.45,d/2)),.07,.42)
x=np.zeros(len(timeline(1.5)))
for start,base in [(0,140),(.24,110),(.52,82)]:place(x,impact(.28,base),start,.5)
u=timeline(1.5);x+=.12*sine(65,u)*np.sin(np.pi*u/1.5)**2
write('sfx','device_repair',fade(x,.005,.3),.08,.45)
# Time travel: intake, low mechanical impact, decompression, long dark tail.
u=timeline(2.8);envelope=np.sin(np.pi*np.minimum(u/.9,1)/2)**2*np.exp(-np.maximum(u-.9,0)*2.0)
x=.20*band(len(u),40,850)*envelope
x+=.45*(sine(48,u)+.22*sine(96,u))*envelope
place(x,impact(1.2,58),.85,.8)
for delay,amp in [(1.02,.25),(1.24,.13)]:place(x,impact(.9,72),delay,amp)
write('sfx','time_travel',fade(x,.12,.65),.12,.62)
# 1.9 s matches the menu's fast-clock animation.
x=np.zeros(len(timeline(1.9)));start=.015;i=0
while start<1.65:
    place(x,tick(i%2==1),start,.6);start+=max(.027,.145*(1-start/2));i+=1
place(x,impact(.23,95),1.66,.32)
write('sfx','clock_spin',fade(x,.003,.08),.055,.38)
(ROOT/'audio_validation.json').write_text(json.dumps(report,indent=2),encoding='utf8')
print('Generated',len(report),'original PCM16 audio assets.')
