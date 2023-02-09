# md2x
Markdown to X(something).

### Run REPL
In bash
1. run: `npx shadow-cljs watch dev` (a terminal)
2. run: `node out/md2x-interim.js` (another terminal)

In vscode
1. Run `Calva: Connect to a Running REPL Server in the Project`
2. Select `shadow-cljs` 
3. Input nREPL IP, Port (If already presented, just presss Enter)
4. Select `:dev`

갑자기 `zc`를 해도 중간 print 결과가 안 나온다. 컴퓨터를 껐다 켰지만 여전하다. 이유를 모르겠다..

### Run REPL(Not work! why??)
In bash(another terminal)
1. run: `npx shadow-cljs watch dev`

In vscode
1. Run `Calva: Connect to a Running REPL Server in the Project`
2. Select `shadow-cljs` 
3. Input nREPL IP, Port (If already presented, just presss Enter)
4. Select `node-repl`

얘는 그냥 안 된다. 잘 되다가 안 되는데 도무지 이유를 모르겠다.