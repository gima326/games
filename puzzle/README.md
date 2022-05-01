### Re-frame でゲームをば（ puzzle ）

頭からドボンと飛び込むシリーズ。<br>
Re-frame の流儀に則って、簡単なブラウザゲームのコードをトレースしたり、作ってみたり。<br>
いろいろ学べるといいな、と。<br><br>

Step0: プロジェクト作成<br>
　　　　動作する最小限の puzzle ゲームを作ること<br>
Step1: puzzle サイズ変更<br>
　　　　レベル３〜６を選択できるようにする<br>
Step2: puzzle 手数の表示<br>
　　　　クリアするまでに要した手数を表示する<br>
Step3: puzzle 時間の表示<br>
　　　　クリアするまでに要した時間を表示する<br>
Step4: puzzle ローカルストレージへのログ追加登録<br>
　　　　クリアするまでに要した手数、時間をローカルストレージへ登録する<br>

Step4-2: puzzle リファクタリング<br>
Step4-3: puzzle リファクタリング２<br>
Step4-4: puzzle リファクタリング３<br>

Step5: puzzle SVG でログを出力<br>

Step5-2: puzzle リファクタリング４<br>
Step5-3: puzzle リファクタリング５<br>

<!--

Step5: puzzle <br>
-->

![todo](https://github.com/gima326/games/blob/main/puzzle/readme_img/step0-1.png)

![todo](https://github.com/gima326/games/blob/main/puzzle/readme_img/step0-2.png)


In `src/re_frame_puzzle`, there's a matching set of files (each small):
```
src/re_frame_puzzle
├── config.cljs
├── core.cljs         <--- entry point, plus history
├── db.cljs           <--- data related  (data layer)
├── subs.cljs         <--- subscription handlers  (query layer)
├── views.cljs        <--- reagent  components (view layer)
└── events.cljs       <--- event handlers (control/update layer)
```

```
[ core.cljs ] ---> [ config.cljs ]
       ├──-------> [ views.cljs  ] ---> [ subs.cljs ]
       |                  |
       |                  ├-----------> [ 関数 re-frame/subscribe() ]
       |                  |                  |
       |                  |                  | 【 views.cljs、subs.cljs から直接 db/default-db を参照せず、
       |                  |                  | 　関数 re-frame/subscribe() を介して re-frame が管理している「状態」を参照する。
       |                  |                  | 　そして subs.cljs で定義されている関数の引数として、その参照値（状態）を渡す。 】
       |                  |                  |
       |                  v                  v
       └──-------> [ events.cljs ] ---> [ db.cljs ]
```

<!--
## References

- 「[Re−frame 入門][1]」<br>
[ `https://qiita.com/snufkon/items/1d409c984faaa3c390a1` ]<br>
- 「[Reagent: Minimalistic React for ClojureScript][2]」<br>
[ `https://reagent-project.github.io/` ]<br>

[1]: https://qiita.com/snufkon/items/1d409c984faaa3c390a1
[2]: https://reagent-project.github.io/
-->
