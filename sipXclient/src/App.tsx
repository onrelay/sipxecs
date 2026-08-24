import { translate } from "@dao/common";

export function App() {

  return (
    <main className="shell">
      <section className="card">
        <p className="eyebrow">SIPX Client</p>
        <h1>Voice-first communications workspace</h1>
        <p>
          This app is provisioned by sipXecs and will host call controls, chat-driven actions,
          and policy-aware assistant features.
        </p>
        <button type="button">{translate("save")}</button>
      </section>
    </main>
  );
}
