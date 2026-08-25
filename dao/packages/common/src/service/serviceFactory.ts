export class ServiceFactory<S> {
  private instance?: S;

  init(service: S): void {
    if (this.instance) {
      throw new Error("Service already initialized");
    }
    this.instance = service;
  }

  get(): S {
    if (!this.instance) {
      throw new Error("Service not initialized");
    }
    return this.instance;
  }

  isInitialized(): boolean {
    return this.instance !== undefined;
  }
}