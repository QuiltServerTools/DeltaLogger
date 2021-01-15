import { InMemoryCacheConfig } from '@apollo/client'

function concatLists(existing: any[] = [], incoming: any[]) {
  // Slicing is necessary because the existing data is
  // immutable, and frozen in development.
  // const merged = existing ? existing.slice(0) : [];
  // for (let i = 0; i < incoming.length; ++i) {
  //   merged[(args?.offset || 0) + i] = incoming[i];
  // }
  return [...existing, ...incoming];
};

const cacheSettings: InMemoryCacheConfig = {
  typePolicies: {
    Query: {
      fields: {
        placements: { keyArgs: [], merge: concatLists },
        players: { keyArgs: [], merge: concatLists },
        transactions: { keyArgs: [], merge: concatLists },
      },
    },
  },
}

export default cacheSettings
