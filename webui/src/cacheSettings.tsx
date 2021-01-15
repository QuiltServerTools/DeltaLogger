import { InMemoryCacheConfig } from '@apollo/client'

const cacheSettings: InMemoryCacheConfig = {
  typePolicies: {
    Query: {
      fields: {
        placements: {
          keyArgs: [],
          merge(existing: any[] = [], incoming: any[]) {
            // Slicing is necessary because the existing data is
            // immutable, and frozen in development.
            // const merged = existing ? existing.slice(0) : [];
            // for (let i = 0; i < incoming.length; ++i) {
            //   merged[(args?.offset || 0) + i] = incoming[i];
            // }
            return [...existing, ...incoming];
          },
        },
      },
    },
  },
}

export default cacheSettings
