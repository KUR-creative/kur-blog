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

### Caution
최상위 위치에 잘못된 표현식이 있을 경우(예: `undeclared-var`) 파일의 모든 표현식이 eval되지 않는다(calva.loadFile이 실패한다) - 이는 오류가 있는 표현식 이전까지의 식들은 모두 정상적으로 eval되는 clj와의 차이점이다.