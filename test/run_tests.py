"""Compile every test (including archive); run only the safe regression manifest."""
from pathlib import Path
import argparse, os, re, subprocess, tempfile, json
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True);p.add_argument('--artifacts-root',type=Path);p.add_argument('--only',nargs='+');a=p.parse_args()
repo=Path(__file__).resolve().parents[1]
artifacts=(a.artifacts_root or repo.with_name(repo.name+'_artifacts')).resolve()
version=re.search(r"default='([0-9]+)'",(repo/'tools/package_windows.py').read_text(encoding='utf-8')).group(1)
classes=artifacts/'build'/f'v{version}-tests'/'organized';classes.mkdir(parents=True,exist_ok=True)
java=Path(a.java_home)/'bin/java.exe';javac=Path(a.java_home)/'bin/javac.exe'
sources=[f for folder in ['src','test','tools/weather'] for f in (repo/folder).rglob('*.java')]
subprocess.run([str(javac),'-encoding','UTF-8','--release','21','-d',str(classes),*[str(f) for f in sources]],cwd=repo,check=True)
print(f'COMPILE PASS: {len(sources)} Java source files',flush=True)
manifest=[n for n in (repo/'test/suite.txt').read_text().splitlines() if n and not n.startswith('#')]
if a.only and set(a.only)-set(manifest):raise SystemExit('Unknown test name in --only')
results=[]
for name in (repo/'test/suite.txt').read_text().splitlines():
 if not name or name.startswith('#') or (a.only and name not in a.only):continue
 source=repo/'test'/f'{name}.java';text=source.read_text(encoding='utf-8-sig');m=re.search(r'package\s+([\w.]+);',text);cls=(m.group(1)+'.' if m else '')+name
 isolated=Path(tempfile.mkdtemp(prefix='timeescape-test-'))
 cmd=[str(java),'-Djava.awt.headless=true',f'-Dtimeescape.settingsDir={isolated / "settings"}',f'-Dtimeescape.saveDir={isolated / "save"}',f'-Dtimeescape.artifactsRoot={artifacts}','-cp',str(classes)+os.pathsep+str(repo/'src'),cls]
 try:
  result=subprocess.run(cmd,cwd=repo,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=90)
  ok=result.returncode==0;output=result.stdout+result.stderr
 except subprocess.TimeoutExpired:
  ok=False;output='TIMEOUT after 90 seconds'
 results.append(dict(test=name,passed=ok,output=output));print(('PASS ' if ok else 'FAIL ')+name,flush=True)
 if not ok:print(output[-3000:],flush=True)
report=artifacts/'outputs/test-validation';report.mkdir(parents=True,exist_ok=True)
(report/('targeted-results.json' if a.only else 'results.json')).write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
current=report/'current-results.json'
prior=json.loads(current.read_text(encoding='utf-8')) if a.only and current.exists() else []
merged={r['test']:r for r in prior};merged.update({r['test']:r for r in results})
current.write_text(json.dumps(list(merged.values()),ensure_ascii=False,indent=2),encoding='utf-8')
print(f'{sum(r["passed"] for r in results)}/{len(results)} passed; {report}',flush=True)
raise SystemExit(0 if all(r['passed'] for r in results) else 1)
