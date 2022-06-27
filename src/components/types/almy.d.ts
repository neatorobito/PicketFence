export = almy;

declare const almy: {
    almy: {
        /**
         * Re-initializes the state store.
         * @returns None
         */
        create(): void;

        /**
         * Creates or updates the value in the state store and dispatches notifications to any subscribed handlers.
         *
         * @param key - The name of the value
         * @param value - The value
         *
         */
        dispatch(key : string, value : any): void;

        /**
         * Retrieves a value from the state store.
         *
         * @param key - The name of the value
         * @returns The value
         *
         */
        state(key : string): any;

        /**
         * Subscribes to dispatched events.
         *
         * @param key - The name of the value
         * @param callback - A function that is called when the value is updated
         *
         * @note If a event has been dispatched before, the callback will be called immediately after subscribing.
         */
        subscribe(key : string, callback : (value : any) => void): void;
    };
};

