import { Box, Code, Heading } from '@chakra-ui/react';
import * as React from 'react'

interface ErrorBoundaryState {
  hasError: boolean;
  errorMessage: string;
}

class ErrorBoundary extends React.Component<{}, ErrorBoundaryState> {
  constructor(props: object) {
    super(props);
    this.state = {
      hasError: false,
      errorMessage: '',
    };
  }

  static getDerivedStateFromError(error: any) {
    // Update state so the next render will show the fallback UI.
    return {
      hasError: true,
      errorMessage: `${error.name}: ${error.message}`,
    };
  }

  componentDidCatch(error: any, errorInfo: any) {
    // You can also log the error to an error reporting service
    // logErrorToMyService(error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      // You can render any custom fallback UI
      return (
        <Box>
          <Heading size="md" color="red.500" mb="4">
            Something went wrong.
          </Heading>
          <Code
            width="100%"
            colorScheme="red"
          >
            { this.state.errorMessage }
          </Code>
        </Box>
      )
    }

    return this.props.children; 
  }
}

export default ErrorBoundary