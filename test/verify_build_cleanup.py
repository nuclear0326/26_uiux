"""Exercise cleanup in disposable fixtures only; never launches the game."""
from pathlib import Path
import tempfile, shutil, subprocess, zipfile
ROOT = Path(__file__).resolve().parents[1]
work = Path(tempfile.mkdtemp(prefix='timeescape-cleanup-'))

def fixture(name):
    root = work/name
    (root/'tools').mkdir(parents=True)
    (root/'build').mkdir()
    (root/'outputs').mkdir()
    (root/'outputs/game.zip').write_bytes(b'keep-deployment')
    shutil.copy2(ROOT/'tools/clean_build.ps1', root/'tools/clean_build.ps1')
    (root/'build/uix_v91.jar').write_bytes(b'old')
    return root

def jar(root, version):
    with zipfile.ZipFile(root/f'build/uix_v{version}.jar', 'w') as z:
        z.writestr('timeescape/Main.class', b'fixture')
        z.writestr('META-INF/MANIFEST.MF', 'Main-Class: timeescape.Main')

def run(root, version):
    return subprocess.run(['powershell.exe','-NoProfile','-ExecutionPolicy','Bypass','-File',str(root/'tools/clean_build.ps1'),'-Version',str(version),'-ArtifactsRoot',str(root)],capture_output=True).returncode

r=fixture('success'); jar(r,92)
for name in ['v91-classes','v91-tests','v92-classes','v92-tests','weather-tools','.staging-abcdef']:
    (r/'build'/name).mkdir(); (r/'build'/name/'data').write_bytes(b'fixture')
(r/'build/personal-notes.txt').write_text('keep unknown files')
assert run(r,92)==0
assert {p.name for p in (r/'build').iterdir()}=={'uix_v92.jar','v92-classes','v92-tests','personal-notes.txt'}
assert (r/'outputs/game.zip').read_bytes()==b'keep-deployment'
assert run(r,92)==0
for name in ['missing','corrupt','incomplete','newer']:
    r=fixture(name)
    if name=='corrupt': (r/'build/uix_v92.jar').write_bytes(b'bad zip')
    if name=='incomplete':
        with zipfile.ZipFile(r/'build/uix_v92.jar','w') as z: z.writestr('wrong.txt','incomplete')
    if name=='newer': jar(r,92); jar(r,93)
    before={p.name:p.read_bytes() for p in (r/'build').iterdir()}
    assert run(r,92)!=0
    assert before=={p.name:p.read_bytes() for p in (r/'build').iterdir()}
    assert (r/'outputs/game.zip').read_bytes()==b'keep-deployment'
print('PASS: latest retained, old removed, unknown/deployment preserved, repeat safe, missing/corrupt/incomplete/newer build guarded')
print('Fixtures:',work)
