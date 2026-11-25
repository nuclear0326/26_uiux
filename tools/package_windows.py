"""Package the built JAR with original external fonts and an existing Java runtime."""
from pathlib import Path
import argparse, hashlib, shutil, zipfile, subprocess

parser=argparse.ArgumentParser()
parser.add_argument('--version',default='92')
parser.add_argument('--base',default='91')
parser.add_argument('--artifacts-root',type=Path)
parser.add_argument('--zip',action='store_true')
args=parser.parse_args()
if not args.version.isdecimal() or not args.base.isdecimal():raise ValueError('Numeric versions required')
root=Path(__file__).resolve().parent.parent
artifacts=(args.artifacts_root or root.with_name(root.name+'_artifacts')).resolve()
base=artifacts/f'outputs/uix_v{args.base}_windows/TimeEscape'
dest=artifacts/f'outputs/uix_v{args.version}_windows/TimeEscape'
if not dest.exists():
    dest.mkdir(parents=True)
    for name in ['TimeEscape.exe','TimeEscape.ico']:shutil.copy2(base/name,dest/name)
    shutil.copytree(base/'runtime',dest/'runtime')
guide=(root/'docs/TEMPORARY_CHEAT.md').read_text(encoding='utf-8')
guide=guide.replace('[미니게임 가이드](MINIGAME_TEAM_GUIDE.md)', '소스 저장소의 docs/MINIGAME_TEAM_GUIDE.md 참고')
(dest/'TEMPORARY_CHEAT.md').write_text(guide,encoding='utf-8')
app=dest/'app';app.mkdir(exist_ok=True)
dialogue=app/'dialogue';dialogue.mkdir(exist_ok=True)
shutil.copy2(root/'src/assets/dialogue/ko.txt',dialogue/'ko.txt')
fonts=app/'fonts';fonts.mkdir(exist_ok=True)
source=artifacts/f'build/uix_v{args.version}.jar'
target=app/source.name
with zipfile.ZipFile(source) as src,zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as out:
    for info in src.infolist():
        if info.filename.startswith('assets/fonts/') and not info.is_dir():
            path=fonts/Path(info.filename).name
            path.write_bytes(src.read(info))
            assert hashlib.sha256(path.read_bytes()).digest()==hashlib.sha256(src.read(info)).digest()
        else:out.writestr(info,src.read(info))
cfg=f'[Application]\napp.classpath=$APPDIR\\{source.name}\napp.mainclass=timeescape.Main\n\n[JavaOptions]\njava-options=-Djpackage.app-version={args.version}.0\n'
(app/'TimeEscape.cfg').write_text(cfg,encoding='utf-8')
assert (dest/'TimeEscape.exe').read_bytes()[:2]==b'MZ'
with zipfile.ZipFile(target) as jar:assert jar.testzip() is None
print(dest)
if args.zip:
    archive=artifacts/f'outputs/TIME_ESCAPE_v{args.version}_Windows.zip'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for path in dest.rglob('*'):
            if path.is_file() and not any(part == "save" for part in path.relative_to(dest).parts) and ".save" not in path.name:
                z.write(path,path.relative_to(dest.parent).as_posix())
    with zipfile.ZipFile(archive) as z:assert z.testzip() is None
    print(archive)

# Cleanup is last: a packaging or ZIP verification failure preserves old builds.
subprocess.run(['powershell.exe', '-NoProfile', '-ExecutionPolicy', 'Bypass',
                '-File', str(root/'tools/clean_build.ps1'), '-Version', args.version, '-ArtifactsRoot', str(artifacts)], check=True)
